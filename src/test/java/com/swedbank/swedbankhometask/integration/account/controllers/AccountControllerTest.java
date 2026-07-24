package com.swedbank.swedbankhometask.integration.account.controllers;

import tools.jackson.databind.ObjectMapper;
import com.swedbank.swedbankhometask.account.AccountService;
import com.swedbank.swedbankhometask.account.controllers.AccountController;
import com.swedbank.swedbankhometask.account.exceptions.AccountNotFoundException;
import com.swedbank.swedbankhometask.account.handlers.AccountExceptionHandler;
import com.swedbank.swedbankhometask.account.mappers.AccountMapperImpl;
import com.swedbank.swedbankhometask.account.models.Account;
import com.swedbank.swedbankhometask.account.models.AccountBalance;
import com.swedbank.swedbankhometask.account.models.Currency;
import com.swedbank.swedbankhometask.common.handlers.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({AccountMapperImpl.class, AccountExceptionHandler.class, GlobalExceptionHandler.class})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @Test
    @DisplayName("POST /accounts creates an account and returns 201 with all balances")
    void createAccountReturnsCreated() throws Exception {
        Account account = account("Alice");
        when(accountService.createAccount("Alice")).thenReturn(account);

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("owner", "Alice"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.owner").value("Alice"))
                .andExpect(jsonPath("$.data.balances.length()").value(4));
    }

    @Test
    @DisplayName("POST /accounts rejects a blank owner with 400")
    void createAccountRejectsBlankOwner() throws Exception {
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("owner", " "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data.owner").exists());
    }

    @Test
    @DisplayName("GET /accounts/{id}/balance returns the account balances")
    void getBalanceReturnsBalances() throws Exception {
        Account account = account("Bob");
        UUID id = account.getId();
        when(accountService.findAccountById(id)).thenReturn(account);

        mockMvc.perform(get("/accounts/{id}/balance", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balances.length()").value(4));
    }

    @Test
    @DisplayName("GET /accounts/{id}/balance returns 404 for an unknown account")
    void getBalanceReturnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(accountService.findAccountById(id)).thenThrow(new AccountNotFoundException(id));

        mockMvc.perform(get("/accounts/{id}/balance", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /accounts/{id}/credit adds money and returns 200")
    void creditReturnsOk() throws Exception {
        Account account = account("Bob");
        UUID id = account.getId();
        when(accountService.credit(eq(id), eq(Currency.EUR), any(BigDecimal.class))).thenReturn(account);

        mockMvc.perform(post("/accounts/{id}/credit", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("currency", "EUR", "amount", "50.00"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /accounts/{id}/credit rejects a non-positive amount with 400")
    void creditRejectsNonPositiveAmount() throws Exception {
        mockMvc.perform(post("/accounts/{id}/credit", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("currency", "EUR", "amount", "-1.00"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private Account account(String owner) {
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setOwner(owner);
        for (Currency currency : Currency.values()) {
            AccountBalance balance = new AccountBalance();
            balance.setAccount(account);
            balance.setCurrency(currency);
            balance.setAmount(BigDecimal.ZERO);
            account.getBalances().add(balance);
        }
        return account;
    }
}
