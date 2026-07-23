package com.swedbank.swedbankhometask.account.dtos;

import java.util.List;
import java.util.UUID;

/**
 * Account view returned to clients, with one balance per currency.
 *
 * @author vinodjohn
 * @since 23.07.2026
 */
public record AccountDto(UUID id, String owner, List<BalanceDto> balances) {
}
