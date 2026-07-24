package com.swedbank.swedbankhometask.unit.account;

import com.swedbank.swedbankhometask.account.exceptions.AccountNotFoundException;
import com.swedbank.swedbankhometask.account.implementations.AccountServiceImpl;
import com.swedbank.swedbankhometask.account.models.Account;
import com.swedbank.swedbankhometask.account.models.AccountBalance;
import com.swedbank.swedbankhometask.account.models.Currency;
import com.swedbank.swedbankhometask.account.repositories.AccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private AccountRepository accountRepository;

    @Test
    @DisplayName("createAccount initialises a zero balance for every currency and persists it")
    void createAccountInitialisesAllCurrencies() {
        when(accountRepository.saveAndFlush(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account account = accountService.createAccount("Alice");

        assertThat(account.getOwner()).isEqualTo("Alice");
        assertThat(account.getBalances()).hasSize(Currency.values().length);
        assertThat(account.getBalances())
                .extracting(AccountBalance::getCurrency)
                .containsExactlyInAnyOrder(Currency.values());
        assertThat(account.getBalances())
                .allSatisfy(balance -> assertThat(balance.getAmount()).isEqualByComparingTo(BigDecimal.ZERO));
        verify(accountRepository).saveAndFlush(account);
    }

    @Test
    @DisplayName("findAccountById returns the account when it exists")
    void findAccountByIdReturnsAccount() throws AccountNotFoundException {
        UUID id = UUID.randomUUID();
        Account account = new Account();
        account.setId(id);
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        assertThat(accountService.findAccountById(id)).isSameAs(account);
    }

    @Test
    @DisplayName("findAccountById throws when the account is missing")
    void findAccountByIdThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findAccountById(id))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    @DisplayName("credit adds the amount to the matching currency balance")
    void creditAddsAmountToCurrencyBalance() throws AccountNotFoundException {
        UUID id = UUID.randomUUID();
        Account account = accountWithBalance(id, Currency.EUR, "10.0000");
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
        when(accountRepository.saveAndFlush(account)).thenReturn(account);

        Account result = accountService.credit(id, Currency.EUR, new BigDecimal("5.50"));

        assertThat(result.balanceOf(Currency.EUR)).isPresent()
                .get()
                .extracting(AccountBalance::getAmount)
                .isEqualTo(new BigDecimal("15.5000"));
        verify(accountRepository).saveAndFlush(account);
    }

    @Test
    @DisplayName("credit throws when the account is missing")
    void creditThrowsWhenAccountMissing() {
        UUID id = UUID.randomUUID();
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.credit(id, Currency.EUR, BigDecimal.ONE))
                .isInstanceOf(AccountNotFoundException.class);
    }

    private Account accountWithBalance(UUID id, Currency currency, String amount) {
        Account account = new Account();
        account.setId(id);
        AccountBalance balance = new AccountBalance();
        balance.setAccount(account);
        balance.setCurrency(currency);
        balance.setAmount(new BigDecimal(amount));
        account.getBalances().add(balance);
        return account;
    }
}
