package com.swedbank.swedbankhometask.integration.api.exceptions;

import java.io.Serial;
import java.text.MessageFormat;

/**
 * Thrown when the external logging system cannot be reached or returns an error.
 *
 * @author vinodjohn
 * @since 24.07.2026
 */
public class ExternalLoggingException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public ExternalLoggingException(String message, Throwable cause) {
        super(MessageFormat.format("External logging call failed for: {0}", message), cause);
    }
}
