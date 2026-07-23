package com.swedbank.swedbankhometask.account.mappers;

import com.swedbank.swedbankhometask.account.dtos.AccountDto;
import com.swedbank.swedbankhometask.account.dtos.BalanceDto;
import com.swedbank.swedbankhometask.account.models.Account;
import com.swedbank.swedbankhometask.account.models.AccountBalance;
import org.mapstruct.Mapper;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Maps account entities to their API representations, ordering balances by currency.
 *
 * @author vinodjohn
 * @since 23.07.2026
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountDto toDto(Account account);

    BalanceDto toDto(AccountBalance balance);

    default List<BalanceDto> toBalanceDtos(Set<AccountBalance> balances) {
        return balances.stream()
                .sorted(Comparator.comparing(AccountBalance::getCurrency))
                .map(this::toDto)
                .toList();
    }
}
