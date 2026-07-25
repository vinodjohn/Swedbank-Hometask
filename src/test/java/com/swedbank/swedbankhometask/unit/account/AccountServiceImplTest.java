package com.swedbank.swedbankhometask.unit.account;

import com.swedbank.swedbankhometask.account.exceptions.AccountNotFoundException;
import com.swedbank.swedbankhometask.account.exceptions.InsufficientFundsException;
import com.swedbank.swedbankhometask.account.exceptions.InvalidExchangeException;
import com.swedbank.swedbankhometask.account.implementations.AccountServiceImpl;
import com.swedbank.swedbankhometask.account.implementations.ExchangeRateProvider;
import com.swedbank.swedbankhometask.account.models.Account;
import com.swedbank.swedbankhometask.account.models.AccountBalance;
import com.swedbank.swedbankhometask.account.models.Currency;
import com.swedbank.swedbankhometask.account.repositories.AccountRepository;
import com.swedbank.swedbankhometask.integration.api.ExternalLoggingService;
import com.swedbank.swedbankhometask.integration.api.exceptions.ExternalLoggingException;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ExternalLoggingService externalLoggingService;

    @Mock
    private ExchangeRateProvider exchangeRateProvider;

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

    @Test
    @DisplayName("debit logs externally then subtracts the amount when funds are sufficient")
    void debitSubtractsWhenSufficient() throws Exception {
        UUID id = UUID.randomUUID();
        Account account = accountWithBalance(id, Currency.EUR, "100.0000");
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
        when(accountRepository.saveAndFlush(account)).thenReturn(account);

        Account result = accountService.debit(id, Currency.EUR, new BigDecimal("30.00"));

        assertThat(result.balanceOf(Currency.EUR)).isPresent()
                .get()
                .extracting(AccountBalance::getAmount)
                .isEqualTo(new BigDecimal("70.0000"));
        verify(externalLoggingService).log(anyString());
        verify(accountRepository).saveAndFlush(account);
    }

    @Test
    @DisplayName("debit throws on insufficient funds and does not persist")
    void debitThrowsOnInsufficientFunds() throws Exception {
        UUID id = UUID.randomUUID();
        Account account = accountWithBalance(id, Currency.EUR, "10.0000");
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.debit(id, Currency.EUR, new BigDecimal("50.00")))
                .isInstanceOf(InsufficientFundsException.class);
        verify(externalLoggingService).log(anyString());
        verify(accountRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("debit aborts without persisting when external logging fails")
    void debitAbortsWhenExternalLoggingFails() throws Exception {
        UUID id = UUID.randomUUID();
        Account account = accountWithBalance(id, Currency.EUR, "100.0000");
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
        doThrow(new ExternalLoggingException("boom", new RuntimeException()))
                .when(externalLoggingService).log(anyString());

        assertThatThrownBy(() -> accountService.debit(id, Currency.EUR, new BigDecimal("10.00")))
                .isInstanceOf(ExternalLoggingException.class);
        verify(accountRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("debit throws when the account is missing without calling external logging")
    void debitThrowsWhenAccountMissing() {
        UUID id = UUID.randomUUID();
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.debit(id, Currency.EUR, BigDecimal.ONE))
                .isInstanceOf(AccountNotFoundException.class);
        verifyNoInteractions(externalLoggingService);
    }

    @Test
    @DisplayName("exchange moves the converted amount between the two balances")
    void exchangeMovesConvertedAmount() throws Exception {
        UUID id = UUID.randomUUID();
        Account account = accountWithBalance(id, Currency.EUR, "100.0000");
        account.getBalances().add(balance(account, Currency.USD, "0.0000"));
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));
        when(accountRepository.saveAndFlush(account)).thenReturn(account);
        when(exchangeRateProvider.convert(Currency.EUR, Currency.USD, new BigDecimal("40.00")))
                .thenReturn(new BigDecimal("43.6000"));

        Account result = accountService.exchange(id, Currency.EUR, Currency.USD, new BigDecimal("40.00"));

        assertThat(result.balanceOf(Currency.EUR)).get()
                .extracting(AccountBalance::getAmount).isEqualTo(new BigDecimal("60.0000"));
        assertThat(result.balanceOf(Currency.USD)).get()
                .extracting(AccountBalance::getAmount).isEqualTo(new BigDecimal("43.6000"));
        verify(accountRepository).saveAndFlush(account);
    }

    @Test
    @DisplayName("exchange rejects the same source and target currency")
    void exchangeRejectsSameCurrency() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> accountService.exchange(id, Currency.EUR, Currency.EUR, BigDecimal.TEN))
                .isInstanceOf(InvalidExchangeException.class);
        verifyNoInteractions(accountRepository, exchangeRateProvider);
    }

    @Test
    @DisplayName("exchange throws on insufficient source funds and does not persist")
    void exchangeThrowsOnInsufficientFunds() {
        UUID id = UUID.randomUUID();
        Account account = accountWithBalance(id, Currency.EUR, "10.0000");
        when(accountRepository.findById(id)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.exchange(id, Currency.EUR, Currency.USD, new BigDecimal("50.00")))
                .isInstanceOf(InsufficientFundsException.class);
        verify(accountRepository, never()).saveAndFlush(any());
        verifyNoInteractions(exchangeRateProvider);
    }

    @Test
    @DisplayName("exchange throws when the account is missing")
    void exchangeThrowsWhenAccountMissing() {
        UUID id = UUID.randomUUID();
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.exchange(id, Currency.EUR, Currency.USD, BigDecimal.ONE))
                .isInstanceOf(AccountNotFoundException.class);
    }

    private Account accountWithBalance(UUID id, Currency currency, String amount) {
        Account account = new Account();
        account.setId(id);
        account.getBalances().add(balance(account, currency, amount));
        return account;
    }

    private AccountBalance balance(Account account, Currency currency, String amount) {
        AccountBalance balance = new AccountBalance();
        balance.setAccount(account);
        balance.setCurrency(currency);
        balance.setAmount(new BigDecimal(amount));
        return balance;
    }
}
