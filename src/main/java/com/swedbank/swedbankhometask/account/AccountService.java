package com.swedbank.swedbankhometask.account;

import com.swedbank.swedbankhometask.account.exceptions.AccountNotFoundException;
import com.swedbank.swedbankhometask.account.exceptions.InsufficientFundsException;
import com.swedbank.swedbankhometask.account.exceptions.InvalidExchangeException;
import com.swedbank.swedbankhometask.account.models.Account;
import com.swedbank.swedbankhometask.account.models.Currency;
import com.swedbank.swedbankhometask.integration.api.exceptions.ExternalLoggingException;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service for managing bank accounts and their per-currency balances.
 *
 * @author vinodjohn
 * @since 23.07.2026
 */
public interface AccountService {

    /**
     * Opens a new account with a zero balance in every supported currency.
     *
     * @param owner name of the account holder
     * @return the created account
     */
    Account createAccount(String owner);

    /**
     * Finds an account by its identifier.
     *
     * @param id the account identifier
     * @return the matching account
     * @throws AccountNotFoundException if no account exists with the given identifier
     */
    Account findAccountById(UUID id) throws AccountNotFoundException;

    /**
     * Adds money to a single currency balance of an account.
     *
     * @param id       the account identifier
     * @param currency the currency to credit
     * @param amount   the positive amount to add
     * @return the updated account
     * @throws AccountNotFoundException if no account exists with the given identifier
     */
    Account credit(UUID id, Currency currency, BigDecimal amount) throws AccountNotFoundException;

    /**
     * Debits money from a single currency balance of an account. The external logging system is
     * called before the balance is changed; if that call fails, nothing is debited.
     *
     * @param id       the account identifier
     * @param currency the currency to debit
     * @param amount   the positive amount to subtract
     * @return the updated account
     * @throws AccountNotFoundException   if no account exists with the given identifier
     * @throws InsufficientFundsException if the balance is too low to cover the amount
     * @throws ExternalLoggingException   if the external logging call fails
     */
    Account debit(UUID id, Currency currency, BigDecimal amount)
            throws AccountNotFoundException, InsufficientFundsException, ExternalLoggingException;

    /**
     * Exchanges money between two currency balances of the same account at the fixed rates.
     *
     * @param id     the account identifier
     * @param from   the source currency to take money from
     * @param to     the target currency to add money to
     * @param amount the positive amount, in the source currency, to exchange
     * @return the updated account
     * @throws AccountNotFoundException   if no account exists with the given identifier
     * @throws InsufficientFundsException if the source balance is too low to cover the amount
     * @throws InvalidExchangeException   if the source and target currencies are the same
     */
    Account exchange(UUID id, Currency from, Currency to, BigDecimal amount)
            throws AccountNotFoundException, InsufficientFundsException, InvalidExchangeException;
}
