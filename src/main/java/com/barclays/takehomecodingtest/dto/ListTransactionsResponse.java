package com.barclays.takehomecodingtest.dto;

import java.util.List;

public record ListTransactionsResponse(
        List<TransactionResponse> transactions) {
}
