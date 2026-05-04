package com.barclays.takehomecodingtest.service;

import com.barclays.takehomecodingtest.dto.CreateAccountRequest;
import com.barclays.takehomecodingtest.dto.SortCode;
import com.barclays.takehomecodingtest.model.AccountEntity;
import com.barclays.takehomecodingtest.model.UserEntity;
import com.barclays.takehomecodingtest.repository.AccountRepository;
import com.barclays.takehomecodingtest.utils.UserHelper;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserService userService;

    public AccountService(AccountRepository accountRepository, UserService userService) {
        this.accountRepository = accountRepository;
        this.userService = userService;
    }

    public List<AccountEntity> listAccounts(String userId) {
        return accountRepository.findByUserId(userId);
    }

    public AccountEntity createAccount(CreateAccountRequest accountData, String userId) {
        UserEntity user = userService.getUserById(userId, userId);
        AccountEntity accountEntity = new AccountEntity(
                accountData.name(),
                accountData.accountType(),
                user,
                SortCode.DEFAULT);
        return accountRepository.save(accountEntity);
    }

    public AccountEntity getAccountByAccountNumber(String accountNumber, String authenticatedUserId) {
        AccountEntity account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(
                        () -> new AccountNotFoundException("Account not found with account number: " + accountNumber));

        UserHelper.isUserAuthorisedForAccount(account, authenticatedUserId);
        return account;
    }
}