package com.swedbank.swedbankhometask.common.dtos;

/**
 * Common response envelope returned by every endpoint.
 *
 * @author vinodjohn
 * @since 22.07.2026
 */
public record GenericResponse<T>(boolean success, String message, T data) {
}
