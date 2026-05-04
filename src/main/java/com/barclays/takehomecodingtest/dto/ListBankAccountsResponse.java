package com.barclays.takehomecodingtest.dto;

import java.util.List;

import com.barclays.takehomecodingtest.model.AccountEntity;

public record ListBankAccountsResponse(
        List<AccountEntity> accounts) {
}
