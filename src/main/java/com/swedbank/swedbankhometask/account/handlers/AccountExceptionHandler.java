package com.swedbank.swedbankhometask.account.handlers;

import com.swedbank.swedbankhometask.account.exceptions.AccountNotFoundException;
import com.swedbank.swedbankhometask.account.exceptions.InsufficientFundsException;
import com.swedbank.swedbankhometask.account.exceptions.InvalidExchangeException;
import com.swedbank.swedbankhometask.common.dtos.GenericResponse;
import com.swedbank.swedbankhometask.integration.api.exceptions.ExternalLoggingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates account domain exceptions into HTTP responses.
 *
 * @author vinodjohn
 * @since 23.07.2026
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.swedbank.swedbankhometask.account")
public class AccountExceptionHandler {
    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public GenericResponse<Void> handleNotFound(AccountNotFoundException ex) {
        return new GenericResponse<>(false, ex.getMessage(), null);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public GenericResponse<Void> handleInsufficientFunds(InsufficientFundsException ex) {
        return new GenericResponse<>(false, ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidExchangeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GenericResponse<Void> handleInvalidExchange(InvalidExchangeException ex) {
        return new GenericResponse<>(false, ex.getMessage(), null);
    }

    @ExceptionHandler(ExternalLoggingException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public GenericResponse<Void> handleExternalLogging(ExternalLoggingException ex) {
        log.warn("External logging failed, debit aborted: {}", ex.getMessage());
        return new GenericResponse<>(false, ex.getMessage(), null);
    }
}
