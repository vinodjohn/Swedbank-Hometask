package com.swedbank.swedbankhometask.account.dtos;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to open a new account.
 *
 * @author vinodjohn
 * @since 23.07.2026
 */
public record CreateAccountRequest(@NotBlank String owner) {
}
