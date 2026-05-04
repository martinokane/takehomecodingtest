package com.barclays.takehomecodingtest.controllers;

import com.barclays.takehomecodingtest.dto.CreateAccountRequest;
import com.barclays.takehomecodingtest.dto.ListBankAccountsResponse;
import com.barclays.takehomecodingtest.model.AccountEntity;
import com.barclays.takehomecodingtest.security.JwtTokenProvider;
import com.barclays.takehomecodingtest.service.AccountService;
import com.barclays.takehomecodingtest.utils.AuthHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "accounts", description = "Manage a bank account")
@RequestMapping("/${api.version}/accounts")
public class AccountController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;
    private final JwtTokenProvider jwtTokenProvider;

    public AccountController(AccountService accountService, JwtTokenProvider jwtTokenProvider) {
        this.accountService = accountService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping
    @Operation(summary = "List accounts", description = "Retrieves a list of all bank accounts for the authenticated user.")
    public ResponseEntity<ListBankAccountsResponse> listAccounts(HttpServletRequest request) {
        String authenticatedUserId = AuthHelper.getAuthenticatedUserIdFromRequest(request, jwtTokenProvider);
        logger.info("Fetching all accounts for user id: {}", authenticatedUserId);
        List<AccountEntity> accounts = accountService.listAccounts(authenticatedUserId);
        return ResponseEntity.ok(new ListBankAccountsResponse(accounts));
    }

    @GetMapping("/{accountNumber}")
    @Operation(summary = "Get account", description = "Retrieves the details of a specific bank account by account number for the authenticated user.")
    public ResponseEntity<AccountEntity> getAccount(@PathVariable String accountNumber, HttpServletRequest request) {
        String authenticatedUserId = AuthHelper.getAuthenticatedUserIdFromRequest(request, jwtTokenProvider);
        logger.info("Fetching account with number: {} for user id: {}", accountNumber, authenticatedUserId);
        AccountEntity account = accountService.getAccountByAccountNumber(accountNumber, authenticatedUserId);
        return ResponseEntity.ok(account);
    }

    @PostMapping
    @Operation(summary = "Create account", description = "Creates a new bank account for the authenticated user.")
    public ResponseEntity<AccountEntity> createAccount(@Valid @RequestBody CreateAccountRequest createAccountRequest,
            HttpServletRequest request) {
        String authenticatedUserId = AuthHelper.getAuthenticatedUserIdFromRequest(request, jwtTokenProvider);
        logger.info("Creating a new account for user id: {} with name: {}", authenticatedUserId,
                createAccountRequest.name());

        AccountEntity account = accountService.createAccount(createAccountRequest, authenticatedUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }
}
