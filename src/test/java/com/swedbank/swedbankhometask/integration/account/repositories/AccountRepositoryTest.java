package com.swedbank.swedbankhometask.integration.account.repositories;

import com.swedbank.swedbankhometask.account.models.Account;
import com.swedbank.swedbankhometask.account.models.AccountBalance;
import com.swedbank.swedbankhometask.account.models.Currency;
import com.swedbank.swedbankhometask.account.repositories.AccountRepository;
import com.swedbank.swedbankhometask.common.configs.JpaAuditingConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaAuditingConfiguration.class)
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("persists an account together with its balances and audit metadata")
    void persistsAccountWithBalancesAndAuditData() {
        Account account = new Account();
        account.setOwner("Alice");
        account.getBalances().add(balance(account, Currency.EUR, "100.0000"));
        account.getBalances().add(balance(account, Currency.USD, "0.0000"));

        Account saved = accountRepository.saveAndFlush(account);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isZero();
        assertThat(saved.getCreatedDate()).isNotNull();
        assertThat(saved.getCreatedBy()).isEqualTo("system");
        assertThat(saved.getBalances()).hasSize(2);
    }

    @Test
    @DisplayName("reloads the persisted balances with their currencies and amounts")
    void reloadsPersistedBalances() {
        Account account = new Account();
        account.setOwner("Bob");
        account.getBalances().add(balance(account, Currency.SEK, "42.5000"));
        UUID id = accountRepository.saveAndFlush(account).getId();
        accountRepository.flush();

        Account reloaded = accountRepository.findById(id).orElseThrow();

        assertThat(reloaded.balanceOf(Currency.SEK))
                .isPresent()
                .get()
                .extracting(AccountBalance::getAmount)
                .isEqualTo(new BigDecimal("42.5000"));
    }

    private AccountBalance balance(Account account, Currency currency, String amount) {
        AccountBalance balance = new AccountBalance();
        balance.setAccount(account);
        balance.setCurrency(currency);
        balance.setAmount(new BigDecimal(amount));
        return balance;
    }
}
