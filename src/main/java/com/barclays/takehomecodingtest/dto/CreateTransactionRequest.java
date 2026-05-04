package com.barclays.takehomecodingtest.dto;

import java.util.Currency;

import jakarta.validation.constraints.NotNull;

public record CreateTransactionRequest(
        @NotNull Double amount,
        @NotNull Currency currency,
        @NotNull TransactionType type,
        String reference) {
}