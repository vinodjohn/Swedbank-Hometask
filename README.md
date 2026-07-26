# swedbank-hometask

A small REST service for bank accounts that hold balances in several currencies. You can
add money, take money out, check the balances and exchange value between currencies at
fixed rates. It all runs in one process against an H2 database that lives in memory, so
there is nothing to install or start up first.

## Stack

* Java 25, Spring Boot 4.1
* Spring Web MVC and Spring Data JPA
* Spring Modulith for the module layout
* H2 (in memory), Lombok, MapStruct
* Gradle

## Running

```bash
./gradlew bootRun
```

The service comes up on `http://localhost:8080`. The H2 console sits at
`http://localhost:8080/h2-console` if you want to poke at the data (JDBC URL `jdbc:h2:mem:swedbank`,
user `sa`, empty password). Swagger UI is at `http://localhost:8080/swagger-ui.html` and the raw
OpenAPI document at `http://localhost:8080/v3/api-docs`.

To compile everything and run the tests:

```bash
./gradlew build
```

## Domain

An account belongs to one owner and keeps a separate balance for each currency. Four
currencies are supported, `EUR`, `USD`, `SEK` and `GBP`, and a brand new account starts
at `0.0000` in every one of them.

**Credit** adds a positive amount to a single currency balance.

**Debit** takes money out of a single currency balance. Nothing is converted automatically,
so a debit only touches the currency you asked for. Just before the balance changes, the
service calls out to an external logging system. If that call does not succeed the whole
debit is abandoned and nothing is written.

**Exchange** moves value between two balances on the same account using the configured
rates. You cannot exchange a currency into itself, and the source balance has to be big
enough to cover the amount.

Rates live in `application.properties` as the number of units per one EUR. A conversion
works out to `amount * rate(to) / rate(from)`, rounded half up to four decimals:

```properties
exchange.rates.EUR=1.0
exchange.rates.USD=1.09
exchange.rates.SEK=11.35
exchange.rates.GBP=0.85
```

The logging call is just a GET to a URL you can configure (mock.httpstatus.io out of the box,
a reliable stand in for httpstat.us). You can change the status code it hands back to pretend
the dependency is failing:

```properties
external.logging.base-url=https://mock.httpstatus.io
external.logging.status-code=200
external.logging.timeout-ms=3000
```

## API

Every response comes back wrapped as `{ "success": ..., "message": ..., "data": ... }`.

| Method | Path                       | Purpose                          |
|--------|----------------------------|----------------------------------|
| POST   | `/accounts`                | Open a new account               |
| GET    | `/accounts/{id}/balance`   | Read all balances of an account  |
| POST   | `/accounts/{id}/credit`    | Add money to one currency        |
| POST   | `/accounts/{id}/debit`     | Take money from one currency     |
| POST   | `/accounts/{id}/exchange`  | Convert between two currencies   |

### Examples

Open an account:

```bash
curl -X POST http://localhost:8080/accounts \
  -H 'Content-Type: application/json' \
  -d '{"owner": "Alice"}'
```

Add money:

```bash
curl -X POST http://localhost:8080/accounts/{id}/credit \
  -H 'Content-Type: application/json' \
  -d '{"currency": "EUR", "amount": 100.00}'
```

Take money out:

```bash
curl -X POST http://localhost:8080/accounts/{id}/debit \
  -H 'Content-Type: application/json' \
  -d '{"currency": "EUR", "amount": 40.00}'
```

Exchange between currencies:

```bash
curl -X POST http://localhost:8080/accounts/{id}/exchange \
  -H 'Content-Type: application/json' \
  -d '{"fromCurrency": "EUR", "toCurrency": "USD", "amount": 40.00}'
```

Read the balances:

```bash
curl http://localhost:8080/accounts/{id}/balance
```

### Error responses

| Situation                         | Status |
|-----------------------------------|--------|
| Invalid request body              | 400    |
| Exchange into the same currency   | 400    |
| Account not found                 | 404    |
| Not enough balance                | 409    |
| External logging call failed      | 502    |

## Structure

The code is a Spring Modulith monolith broken into three modules under
`com.swedbank.swedbankhometask`:

* **account** holds the account aggregate and the credit, debit, exchange and balance operations.
* **integration** owns the external logging call and only lets other modules reach it through its `api` package.
* **common** keeps the shared bits: the auditing base class, the response wrapper and the global exception handling.

Inside a module the layers are the usual ones. There is a service interface at the module
root with its implementation tucked away in `implementations`, JPA entities in `models`,
request and response records in `dtos`, plus controllers, repositories and mappers. A test
called `ModularityTest` makes sure nobody reaches across the module boundaries.

## Tests

There are three layers of tests and they mirror the source packages:

* **unit** covers the service logic, the exchange maths and the mapper with Mockito.
* **integration** covers the web layer with `@WebMvcTest`, the repository with `@DataJpaTest` and the external call against a mocked REST server.
* **e2e** drives the whole application over HTTP with `@SpringBootTest` on a random port.

Run the lot with `./gradlew test`.

## Notes and trade offs

A few decisions worth calling out:

Balances are `BigDecimal` with a scale of four everywhere. Money never touches `double`.

The account is the consistency boundary and carries a `@Version` for optimistic locking, so
two updates landing on the same account at once cannot quietly clobber each other.

The logging call happens inside the debit transaction and before any balance moves, so a
failure leaves the account exactly as it was. In a real system I would probably make that
call idempotent or push it onto an outbox, but for this exercise the plain synchronous check
does the job.

Exchange rates are fixed in configuration. If they ever needed to come from a live source,
the only thing to touch would be `ExchangeRateProvider`.
