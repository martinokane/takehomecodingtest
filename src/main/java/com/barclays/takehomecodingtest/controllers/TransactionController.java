package com.barclays.takehomecodingtest.controllers;

import com.barclays.takehomecodingtest.dto.CreateTransactionRequest;
import com.barclays.takehomecodingtest.dto.ListTransactionsResponse;
import com.barclays.takehomecodingtest.dto.TransactionResponse;
import com.barclays.takehomecodingtest.model.TransactionEntity;
import com.barclays.takehomecodingtest.security.JwtTokenProvider;
import com.barclays.takehomecodingtest.service.TransactionService;
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
@Tag(name = "transactions", description = "Manage transactions on a bank account")
@RequestMapping("/${api.version}/accounts/{accountNumber}/transactions")
public class TransactionController {
        private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

        private final TransactionService transactionService;
        private final JwtTokenProvider jwtTokenProvider;

        public TransactionController(TransactionService transactionService, JwtTokenProvider jwtTokenProvider) {
                this.transactionService = transactionService;
                this.jwtTokenProvider = jwtTokenProvider;
        }

        @GetMapping
        @Operation(summary = "List transactions", description = "Retrieves a list of all transactions for a specific bank account for the authenticated user.")
        public ResponseEntity<ListTransactionsResponse> listTransactions(@PathVariable String accountNumber,
                        HttpServletRequest request) {
                String authenticatedUserId = AuthHelper.getAuthenticatedUserIdFromRequest(request, jwtTokenProvider);
                logger.info("Fetching all transactions for user id: {} for account number: {}", authenticatedUserId,
                                accountNumber);
                List<TransactionEntity> transactions = transactionService.listTransactions(accountNumber,
                                authenticatedUserId);
                return ResponseEntity.ok(new ListTransactionsResponse(transactions.stream()
                                .map(t -> new TransactionResponse(
                                                t.getId(),
                                                t.getAmount().doubleValue(),
                                                t.getCurrency(),
                                                t.getType(),
                                                t.getReference(),
                                                t.getUser().getId(),
                                                t.getCreatedAt()))
                                .toList()));
        }

        @PostMapping
        @Operation(summary = "Create transaction", description = "Creates a new transaction (deposit or withdrawal) for a specific bank account for the authenticated user.")
        public ResponseEntity<TransactionResponse> createTransaction(@PathVariable String accountNumber,
                        @Valid @RequestBody CreateTransactionRequest createTransactionRequest,
                        HttpServletRequest request) {
                String authenticatedUserId = AuthHelper.getAuthenticatedUserIdFromRequest(request, jwtTokenProvider);
                logger.info("Creating a new transaction for user id: {} for account number: {}", authenticatedUserId,
                                accountNumber);

                TransactionEntity transaction = transactionService.createTransaction(createTransactionRequest,
                                accountNumber, authenticatedUserId);
                return ResponseEntity.status(HttpStatus.CREATED).body(new TransactionResponse(
                                transaction.getId(),
                                transaction.getAmount().doubleValue(),
                                transaction.getCurrency(),
                                transaction.getType(),
                                transaction.getReference(),
                                transaction.getUser().getId(),
                                transaction.getCreatedAt()));
        }
}
