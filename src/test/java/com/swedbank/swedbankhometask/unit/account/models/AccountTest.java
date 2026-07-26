package com.swedbank.swedbankhometask.unit.account.models;

import com.swedbank.swedbankhometask.account.models.Account;
import com.swedbank.swedbankhometask.account.models.AccountBalance;
import com.swedbank.swedbankhometask.account.models.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AccountTest {
    @Test
    @DisplayName("balanceOf returns the balance matching the requested currency")
    void balanceOfReturnsMatchingCurrency() {
        Account account = new Account();
        account.getBalances().add(balance(account, Currency.EUR, "10.00"));
        account.getBalances().add(balance(account, Currency.USD, "5.00"));

        assertThat(account.balanceOf(Currency.USD))
                .isPresent()
                .get()
                .extracting(AccountBalance::getAmount)
                .isEqualTo(new BigDecimal("5.00"));
    }

    @Test
    @DisplayName("balanceOf returns empty when the currency is not held")
    void balanceOfReturnsEmptyWhenAbsent() {
        Account account = new Account();
        account.getBalances().add(balance(account, Currency.EUR, "10.00"));

        assertThat(account.balanceOf(Currency.GBP)).isEmpty();
    }

    // PRIVATE METHODS //
    private AccountBalance balance(Account account, Currency currency, String amount) {
        AccountBalance balance = new AccountBalance();
        balance.setAccount(account);
        balance.setCurrency(currency);
        balance.setAmount(new BigDecimal(amount));
        return balance;
    }
}
