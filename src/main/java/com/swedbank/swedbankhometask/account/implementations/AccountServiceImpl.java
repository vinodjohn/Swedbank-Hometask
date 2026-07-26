package com.swedbank.swedbankhometask.account.implementations;

import com.swedbank.swedbankhometask.account.AccountService;
import com.swedbank.swedbankhometask.account.exceptions.AccountNotFoundException;
import com.swedbank.swedbankhometask.account.exceptions.InsufficientFundsException;
import com.swedbank.swedbankhometask.account.exceptions.InvalidExchangeException;
import com.swedbank.swedbankhometask.account.models.Account;
import com.swedbank.swedbankhometask.account.models.AccountBalance;
import com.swedbank.swedbankhometask.account.models.Currency;
import com.swedbank.swedbankhometask.account.repositories.AccountRepository;
import com.swedbank.swedbankhometask.integration.api.ExternalLoggingService;
import com.swedbank.swedbankhometask.integration.api.exceptions.ExternalLoggingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.UUID;

/**
 * Implementation of {@link AccountService}.
 *
 * @author vinodjohn
 * @since 23.07.2026
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private static final int SCALE = 4;
    private static final BigDecimal ZERO_BALANCE = BigDecimal.ZERO.setScale(SCALE, RoundingMode.UNNECESSARY);

    private final AccountRepository accountRepository;
    private final ExternalLoggingService externalLoggingService;
    private final ExchangeRateProvider exchangeRateProvider;

    @Override
    public Account createAccount(String owner) {
        Account account = new Account();
        account.setOwner(owner);

        for (Currency currency : Currency.values()) {
            account.getBalances().add(newBalance(account, currency));
        }

        log.info("Creating account for owner: {}", owner);
        return accountRepository.saveAndFlush(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Account findAccountById(UUID id) throws AccountNotFoundException {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Override
    public Account credit(UUID id, Currency currency, BigDecimal amount) throws AccountNotFoundException {
        Account account = findAccountById(id);
        AccountBalance balance = balanceFor(account, currency);
        balance.setAmount(balance.getAmount().add(amount).setScale(SCALE, RoundingMode.HALF_UP));

        log.info("Crediting {} {} to account: {}", amount, currency, id);
        return accountRepository.saveAndFlush(account);
    }

    @Override
    public Account debit(UUID id, Currency currency, BigDecimal amount)
            throws AccountNotFoundException, InsufficientFundsException, ExternalLoggingException {
        Account account = findAccountById(id);
        externalLoggingService.log(MessageFormat.format("Debit {0} {1} from account {2}", amount, currency, id));

        AccountBalance balance = balanceFor(account, currency);
        if (balance.getAmount().compareTo(amount) < 0) {
            throw new InsufficientFundsException(id, currency);
        }
        balance.setAmount(balance.getAmount().subtract(amount).setScale(SCALE, RoundingMode.HALF_UP));

        log.info("Debiting {} {} from account: {}", amount, currency, id);
        return accountRepository.saveAndFlush(account);
    }

    @Override
    public Account exchange(UUID id, Currency from, Currency to, BigDecimal amount)
            throws AccountNotFoundException, InsufficientFundsException, InvalidExchangeException {
        if (from == to) {
            throw new InvalidExchangeException(from);
        }

        Account account = findAccountById(id);
        AccountBalance source = balanceFor(account, from);
        if (source.getAmount().compareTo(amount) < 0) {
            throw new InsufficientFundsException(id, from);
        }

        BigDecimal converted = exchangeRateProvider.convert(from, to, amount);
        AccountBalance target = balanceFor(account, to);
        source.setAmount(source.getAmount().subtract(amount).setScale(SCALE, RoundingMode.HALF_UP));
        target.setAmount(target.getAmount().add(converted).setScale(SCALE, RoundingMode.HALF_UP));

        log.info("Exchanging {} {} to {} {} on account: {}", amount, from, converted, to, id);
        return accountRepository.saveAndFlush(account);
    }

    // PRIVATE METHODS //
    private AccountBalance newBalance(Account account, Currency currency) {
        AccountBalance balance = new AccountBalance();
        balance.setAccount(account);
        balance.setCurrency(currency);
        balance.setAmount(ZERO_BALANCE);
        return balance;
    }

    private AccountBalance balanceFor(Account account, Currency currency) {
        return account.balanceOf(currency).orElseGet(() -> {
            AccountBalance balance = newBalance(account, currency);
            account.getBalances().add(balance);
            return balance;
        });
    }
}
