package com.swedbank.swedbankhometask.account.controllers;

import com.swedbank.swedbankhometask.account.AccountService;
import com.swedbank.swedbankhometask.account.dtos.AccountDto;
import com.swedbank.swedbankhometask.account.dtos.CreateAccountRequest;
import com.swedbank.swedbankhometask.account.dtos.CreditRequest;
import com.swedbank.swedbankhometask.account.dtos.DebitRequest;
import com.swedbank.swedbankhometask.account.dtos.ExchangeRequest;
import com.swedbank.swedbankhometask.account.exceptions.AccountNotFoundException;
import com.swedbank.swedbankhometask.account.exceptions.InsufficientFundsException;
import com.swedbank.swedbankhometask.account.exceptions.InvalidExchangeException;
import com.swedbank.swedbankhometask.account.mappers.AccountMapper;
import com.swedbank.swedbankhometask.common.dtos.GenericResponse;
import com.swedbank.swedbankhometask.integration.api.exceptions.ExternalLoggingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST controller for account operations.
 *
 * @author vinodjohn
 * @since 23.07.2026
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @PostMapping
    public ResponseEntity<GenericResponse<AccountDto>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountDto account = accountMapper.toDto(accountService.createAccount(request.owner()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GenericResponse<>(true, "Account created successfully", account));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<GenericResponse<AccountDto>> getBalance(@PathVariable UUID id)
            throws AccountNotFoundException {
        AccountDto account = accountMapper.toDto(accountService.findAccountById(id));
        return ResponseEntity.ok(new GenericResponse<>(true, "Balance fetched successfully", account));
    }

    @PostMapping("/{id}/credit")
    public ResponseEntity<GenericResponse<AccountDto>> credit(@PathVariable UUID id,
                                                              @Valid @RequestBody CreditRequest request)
            throws AccountNotFoundException {
        AccountDto account = accountMapper.toDto(accountService.credit(id, request.currency(), request.amount()));
        return ResponseEntity.ok(new GenericResponse<>(true, "Account credited successfully", account));
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<GenericResponse<AccountDto>> debit(@PathVariable UUID id,
                                                             @Valid @RequestBody DebitRequest request)
            throws AccountNotFoundException, InsufficientFundsException, ExternalLoggingException {
        AccountDto account = accountMapper.toDto(accountService.debit(id, request.currency(), request.amount()));
        return ResponseEntity.ok(new GenericResponse<>(true, "Account debited successfully", account));
    }

    @PostMapping("/{id}/exchange")
    public ResponseEntity<GenericResponse<AccountDto>> exchange(@PathVariable UUID id,
                                                                @Valid @RequestBody ExchangeRequest request)
            throws AccountNotFoundException, InsufficientFundsException, InvalidExchangeException {
        AccountDto account = accountMapper.toDto(
                accountService.exchange(id, request.fromCurrency(), request.toCurrency(), request.amount()));
        return ResponseEntity.ok(new GenericResponse<>(true, "Currency exchanged successfully", account));
    }
}
