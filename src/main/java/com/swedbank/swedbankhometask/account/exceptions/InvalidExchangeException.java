package com.swedbank.swedbankhometask.account.exceptions;

import com.swedbank.swedbankhometask.account.models.Currency;

import java.io.Serial;
import java.text.MessageFormat;

/**
 * Thrown when a currency exchange request is not valid, such as exchanging a currency into itself.
 *
 * @author vinodjohn
 * @since 25.07.2026
 */
public class InvalidExchangeException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidExchangeException(Currency currency) {
        super(MessageFormat.format("Cannot exchange {0} into the same currency!", currency));
    }
}
