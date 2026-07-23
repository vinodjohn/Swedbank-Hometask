package com.swedbank.swedbankhometask.account.implementations;

import com.swedbank.swedbankhometask.account.AccountService;
import com.swedbank.swedbankhometask.account.exceptions.AccountNotFoundException;
import com.swedbank.swedbankhometask.account.models.Account;
import com.swedbank.swedbankhometask.account.models.AccountBalance;
import com.swedbank.swedbankhometask.account.models.Currency;
import com.swedbank.swedbankhometask.account.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private static final BigDecimal ZERO_BALANCE = BigDecimal.ZERO.setScale(4, RoundingMode.UNNECESSARY);

    private final AccountRepository accountRepository;

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

    // PRIVATE METHODS //

    private AccountBalance newBalance(Account account, Currency currency) {
        AccountBalance balance = new AccountBalance();
        balance.setAccount(account);
        balance.setCurrency(currency);
        balance.setAmount(ZERO_BALANCE);
        return balance;
    }
}
