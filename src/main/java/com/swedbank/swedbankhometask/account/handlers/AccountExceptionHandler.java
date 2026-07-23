package com.swedbank.swedbankhometask.account.handlers;

import com.swedbank.swedbankhometask.account.exceptions.AccountNotFoundException;
import com.swedbank.swedbankhometask.common.dtos.GenericResponse;
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
}
