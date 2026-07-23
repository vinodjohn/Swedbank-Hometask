package com.swedbank.swedbankhometask.account;

import com.swedbank.swedbankhometask.account.exceptions.AccountNotFoundException;
import com.swedbank.swedbankhometask.account.models.Account;

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
}
