package com.swedbank.swedbankhometask.e2e.account;

import com.swedbank.swedbankhometask.integration.api.ExternalLoggingService;
import com.swedbank.swedbankhometask.integration.api.exceptions.ExternalLoggingException;
import tools.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureTestRestTemplate
class AccountLifecycleE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private ExternalLoggingService externalLoggingService;

    @Test
    @DisplayName("opens an account and reads back four zero balances")
    void opensAccountAndReadsBalances() {
        ResponseEntity<JsonNode> created = restTemplate.postForEntity(
                "/accounts", Map.of("owner", "Alice"), JsonNode.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().path("success").asBoolean()).isTrue();

        String id = created.getBody().path("data").path("id").asText();
        assertThat(id).isNotBlank();

        ResponseEntity<JsonNode> balance = restTemplate.getForEntity(
                "/accounts/{id}/balance", JsonNode.class, id);

        assertThat(balance.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode balances = balance.getBody().path("data").path("balances");
        assertThat(balances).hasSize(4);
        balances.forEach(node ->
                assertThat(new BigDecimal(node.path("amount").asText())).isEqualByComparingTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("credits an account and reflects the new balance")
    void creditsAccount() {
        String id = restTemplate.postForEntity("/accounts", Map.of("owner", "Carol"), JsonNode.class)
                .getBody().path("data").path("id").asText();

        ResponseEntity<JsonNode> credited = restTemplate.postForEntity(
                "/accounts/{id}/credit", Map.of("currency", "EUR", "amount", "100.00"), JsonNode.class, id);

        assertThat(credited.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eurAmount(credited.getBody())).isEqualByComparingTo("100.00");

        ResponseEntity<JsonNode> balance = restTemplate.getForEntity("/accounts/{id}/balance", JsonNode.class, id);
        assertThat(eurAmount(balance.getBody())).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("debits an account after the external logging call succeeds")
    void debitsAccount() {
        String id = openAccount("Dave");
        restTemplate.postForEntity("/accounts/{id}/credit",
                Map.of("currency", "EUR", "amount", "100.00"), JsonNode.class, id);

        ResponseEntity<JsonNode> debited = restTemplate.postForEntity(
                "/accounts/{id}/debit", Map.of("currency", "EUR", "amount", "40.00"), JsonNode.class, id);

        assertThat(debited.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(eurAmount(debited.getBody())).isEqualByComparingTo("60.00");
    }

    @Test
    @DisplayName("returns 409 when debiting more than the available balance")
    void rejectsDebitOnInsufficientFunds() {
        String id = openAccount("Erin");

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/accounts/{id}/debit", Map.of("currency", "EUR", "amount", "10.00"), JsonNode.class, id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().path("success").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("returns 502 and does not debit when external logging fails")
    void abortsDebitWhenExternalLoggingFails() throws ExternalLoggingException {
        String id = openAccount("Frank");
        restTemplate.postForEntity("/accounts/{id}/credit",
                Map.of("currency", "EUR", "amount", "100.00"), JsonNode.class, id);
        doThrow(new ExternalLoggingException("boom", new RuntimeException()))
                .when(externalLoggingService).log(anyString());

        ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                "/accounts/{id}/debit", Map.of("currency", "EUR", "amount", "40.00"), JsonNode.class, id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        ResponseEntity<JsonNode> balance = restTemplate.getForEntity("/accounts/{id}/balance", JsonNode.class, id);
        assertThat(eurAmount(balance.getBody())).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("exchanges EUR into USD at the configured rate")
    void exchangesBetweenCurrencies() {
        String id = openAccount("Grace");
        restTemplate.postForEntity("/accounts/{id}/credit",
                Map.of("currency", "EUR", "amount", "100.00"), JsonNode.class, id);

        ResponseEntity<JsonNode> exchanged = restTemplate.postForEntity("/accounts/{id}/exchange",
                Map.of("fromCurrency", "EUR", "toCurrency", "USD", "amount", "40.00"), JsonNode.class, id);

        assertThat(exchanged.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(amountOf(exchanged.getBody(), "EUR")).isEqualByComparingTo("60.00");
        assertThat(amountOf(exchanged.getBody(), "USD")).isEqualByComparingTo("43.60");
    }

    @Test
    @DisplayName("returns 400 when exchanging a currency into itself")
    void rejectsExchangeIntoSameCurrency() {
        String id = openAccount("Heidi");
        restTemplate.postForEntity("/accounts/{id}/credit",
                Map.of("currency", "EUR", "amount", "100.00"), JsonNode.class, id);

        ResponseEntity<JsonNode> response = restTemplate.postForEntity("/accounts/{id}/exchange",
                Map.of("fromCurrency", "EUR", "toCurrency", "EUR", "amount", "10.00"), JsonNode.class, id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().path("success").asBoolean()).isFalse();
    }

    @Test
    @DisplayName("returns 404 when reading the balance of an unknown account")
    void returnsNotFoundForUnknownAccount() {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                "/accounts/{id}/balance", JsonNode.class, UUID.randomUUID());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().path("success").asBoolean()).isFalse();
    }

    private String openAccount(String owner) {
        return restTemplate.postForEntity("/accounts", Map.of("owner", owner), JsonNode.class)
                .getBody().path("data").path("id").asText();
    }

    private BigDecimal eurAmount(JsonNode body) {
        return amountOf(body, "EUR");
    }

    private BigDecimal amountOf(JsonNode body, String currency) {
        for (JsonNode balance : body.path("data").path("balances")) {
            if (currency.equals(balance.path("currency").asText())) {
                return new BigDecimal(balance.path("amount").asText());
            }
        }
        throw new AssertionError(currency + " balance not found");
    }
}
