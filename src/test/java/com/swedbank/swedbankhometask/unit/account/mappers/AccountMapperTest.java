package com.swedbank.swedbankhometask.unit.account.mappers;

import com.swedbank.swedbankhometask.account.dtos.AccountDto;
import com.swedbank.swedbankhometask.account.dtos.BalanceDto;
import com.swedbank.swedbankhometask.account.mappers.AccountMapper;
import com.swedbank.swedbankhometask.account.mappers.AccountMapperImpl;
import com.swedbank.swedbankhometask.account.models.Account;
import com.swedbank.swedbankhometask.account.models.AccountBalance;
import com.swedbank.swedbankhometask.account.models.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountMapperTest {

    private final AccountMapper accountMapper = new AccountMapperImpl();

    @Test
    @DisplayName("toDto maps identity and orders balances by currency")
    void toDtoOrdersBalancesByCurrency() {
        UUID id = UUID.randomUUID();
        Account account = new Account();
        account.setId(id);
        account.setOwner("Alice");
        account.getBalances().add(balance(account, Currency.GBP, "3.0000"));
        account.getBalances().add(balance(account, Currency.EUR, "1.0000"));
        account.getBalances().add(balance(account, Currency.USD, "2.0000"));

        AccountDto dto = accountMapper.toDto(account);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.owner()).isEqualTo("Alice");
        assertThat(dto.balances())
                .extracting(BalanceDto::currency)
                .containsExactly(Currency.EUR, Currency.USD, Currency.GBP);
    }

    private AccountBalance balance(Account account, Currency currency, String amount) {
        AccountBalance balance = new AccountBalance();
        balance.setAccount(account);
        balance.setCurrency(currency);
        balance.setAmount(new BigDecimal(amount));
        return balance;
    }
}
