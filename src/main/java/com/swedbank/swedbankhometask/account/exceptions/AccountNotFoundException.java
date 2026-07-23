package com.swedbank.swedbankhometask.account.exceptions;

import java.io.Serial;
import java.text.MessageFormat;
import java.util.UUID;

/**
 * Thrown when no account exists for a requested identifier.
 *
 * @author vinodjohn
 * @since 23.07.2026
 */
public class AccountNotFoundException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public AccountNotFoundException(UUID id) {
        super(MessageFormat.format("Account not found! (ID: {0})", id));
    }
}
