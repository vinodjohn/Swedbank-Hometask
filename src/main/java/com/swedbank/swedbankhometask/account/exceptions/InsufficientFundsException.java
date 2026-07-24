package com.swedbank.swedbankhometask.account.exceptions;

import com.swedbank.swedbankhometask.account.models.Currency;

import java.io.Serial;
import java.text.MessageFormat;
import java.util.UUID;

/**
 * Thrown when an account has too little balance in the requested currency to be debited.
 *
 * @author vinodjohn
 * @since 24.07.2026
 */
public class InsufficientFundsException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public InsufficientFundsException(UUID accountId, Currency currency) {
        super(MessageFormat.format("Insufficient {0} funds in account! (ID: {1})", currency, accountId));
    }
}
