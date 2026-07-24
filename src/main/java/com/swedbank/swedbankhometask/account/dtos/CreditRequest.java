package com.swedbank.swedbankhometask.account.dtos;

import com.swedbank.swedbankhometask.account.models.Currency;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request to add money to a single currency balance.
 *
 * @author vinodjohn
 * @since 24.07.2026
 */
public record CreditRequest(
        @NotNull Currency currency,
        @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal amount) {
}
