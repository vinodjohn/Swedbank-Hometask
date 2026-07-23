package com.swedbank.swedbankhometask.account.dtos;

import com.swedbank.swedbankhometask.account.models.Currency;

import java.math.BigDecimal;

/**
 * A single currency balance of an account.
 *
 * @author vinodjohn
 * @since 23.07.2026
 */
public record BalanceDto(Currency currency, BigDecimal amount) {
}
