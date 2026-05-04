package com.barclays.takehomecodingtest.service;

import com.barclays.takehomecodingtest.dto.CreateTransactionRequest;
import com.barclays.takehomecodingtest.dto.TransactionType;
import com.barclays.takehomecodingtest.model.AccountEntity;
import com.barclays.takehomecodingtest.model.TransactionEntity;
import com.barclays.takehomecodingtest.repository.AccountRepository;
import com.barclays.takehomecodingtest.repository.TransactionRepository;
import com.barclays.takehomecodingtest.utils.UserHelper;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<TransactionEntity> listTransactions(String accountNumber, String authenticatedUserId) {
        AccountEntity account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountNumber));

        UserHelper.isUserAuthorisedForAccount(account, authenticatedUserId);

        return transactionRepository.findByAccountAccountNumber(accountNumber);
    }

    @Transactional
    public TransactionEntity createTransaction(CreateTransactionRequest transactionData, String accountNumber,
            String authenticatedUserId) {
        AccountEntity account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + accountNumber));

        UserHelper.isUserAuthorisedForAccount(account, authenticatedUserId);

        if (transactionData.amount() <= 0) {
            logger.warn("Attempted to create transaction with non-positive amount: {}", transactionData.amount());
            throw new InvalidTransactionException("Transaction amount must be greater than zero");
        }

        BigDecimal transactionAmount = BigDecimal.valueOf(transactionData.amount());

        if (transactionData.type() == TransactionType.withdrawal
                && !hasSufficientFunds(account, transactionAmount)) {
            logger.warn("Attempted to create withdrawal transaction with insufficient funds for account number: {}",
                    accountNumber);
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }

        TransactionEntity transactionEntity = new TransactionEntity(transactionAmount,
                transactionData.currency(),
                transactionData.type(),
                transactionData.reference(), account);

        BigDecimal newBalance = account.getBalance();
        if (transactionData.type() == TransactionType.deposit) {
            newBalance = newBalance.add(transactionAmount);
        } else if (transactionData.type() == TransactionType.withdrawal) {
            newBalance = newBalance.subtract(transactionAmount);
        }
        account.setBalance(newBalance);
        accountRepository.save(account);

        return transactionRepository.save(transactionEntity);
    }

    private boolean hasSufficientFunds(AccountEntity account, BigDecimal withdrawalAmount) {
        return account.getBalance().compareTo(withdrawalAmount) >= 0;
    }
}
