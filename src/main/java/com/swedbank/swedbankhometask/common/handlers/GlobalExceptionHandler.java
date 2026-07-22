package com.swedbank.swedbankhometask.common.handlers;

import com.swedbank.swedbankhometask.common.dtos.GenericResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles validation and otherwise unhandled errors across the application.
 *
 * @author vinodjohn
 * @since 22.07.2026
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GenericResponse<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
        return new GenericResponse<>(false, "Validation failed", fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public GenericResponse<Void> handleUnreadable(HttpMessageNotReadableException ex) {
        return new GenericResponse<>(false, "Malformed or unreadable request body", null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GenericResponse<Void> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return new GenericResponse<>(false, "An unexpected error occurred", null);
    }
}
