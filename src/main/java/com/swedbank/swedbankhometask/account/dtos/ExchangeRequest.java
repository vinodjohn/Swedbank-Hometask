package com.swedbank.swedbankhometask.account.dtos;

import com.swedbank.swedbankhometask.account.models.Currency;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request to exchange money between two currency balances of the same account.
 *
 * @author vinodjohn
 * @since 25.07.2026
 */
public record ExchangeRequest(
        @NotNull Currency fromCurrency,
        @NotNull Currency toCurrency,
        @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal amount) {
}
