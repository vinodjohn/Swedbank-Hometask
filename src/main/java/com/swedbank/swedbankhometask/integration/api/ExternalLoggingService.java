package com.swedbank.swedbankhometask.integration.api;

import com.swedbank.swedbankhometask.integration.api.exceptions.ExternalLoggingException;

/**
 * Sends log entries to an external system, simulating an audit/logging integration.
 *
 * @author vinodjohn
 * @since 24.07.2026
 */
public interface ExternalLoggingService {

    /**
     * Records a message in the external logging system.
     *
     * @param message the message to log
     * @throws ExternalLoggingException if the external system is unreachable or returns an error
     */
    void log(String message) throws ExternalLoggingException;
}
