package com.barclays.takehomecodingtest.dto;

import java.time.LocalDateTime;
import java.util.Currency;

public record TransactionResponse(
        String id,
        Double amount,
        Currency currency,
        TransactionType type,
        String reference,
        String userId,
        LocalDateTime createdTimestamp) {
}
