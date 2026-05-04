package com.barclays.takehomecodingtest.service;

import com.barclays.takehomecodingtest.dto.CreateTransactionRequest;
import com.barclays.takehomecodingtest.dto.TransactionType;
import com.barclays.takehomecodingtest.model.AccountEntity;
import com.barclays.takehomecodingtest.model.TransactionEntity;
import com.barclays.takehomecodingtest.model.UserEntity;
import com.barclays.takehomecodingtest.repository.AccountRepository;
import com.barclays.takehomecodingtest.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private UserEntity user;
    private AccountEntity account;
    private String accountNumber;
    private String userId;
    private Currency gbp;

    @BeforeEach
    void setUp() {
        accountNumber = "ACC123456789";
        userId = "user-123";
        gbp = Currency.getInstance("GBP");

        user = new UserEntity();
        user.setId(userId);

        account = new AccountEntity();
        account.setAccountNumber(accountNumber);
        account.setBalance(BigDecimal.valueOf(1000.00));
        account.setUser(user);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void listTransactions_ShouldReturnTransactions_WhenAccountExists() {
        // Given
        List<TransactionEntity> expectedTransactions = List.of(
                createMockTransaction("tan-ABC12345", BigDecimal.valueOf(100), TransactionType.deposit),
                createMockTransaction("tan-XYZ98765", BigDecimal.valueOf(50), TransactionType.withdrawal));
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountAccountNumber(accountNumber)).thenReturn(expectedTransactions);

        // When
        List<TransactionEntity> result = transactionService.listTransactions(accountNumber, userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedTransactions);
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(transactionRepository).findByAccountAccountNumber(accountNumber);
    }

    @Test
    void listTransactions_ShouldReturnEmpty_WhenAccountHasNoTransactions() {
        // Given
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountAccountNumber(accountNumber)).thenReturn(List.of());

        // When
        List<TransactionEntity> result = transactionService.listTransactions(accountNumber, userId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void listTransactions_ShouldThrowAccountNotFoundException_WhenAccountDoesNotExist() {
        // Given
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.listTransactions(accountNumber, userId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    void createTransaction_ShouldDeposit_WhenDepositTransactionIsValid() {
        // Given
        BigDecimal depositAmount = BigDecimal.valueOf(500.00);
        CreateTransactionRequest request = new CreateTransactionRequest(500.00, gbp, TransactionType.deposit, "Salary");
        BigDecimal expectedBalance = BigDecimal.valueOf(1500.00);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity arg = invocation.getArgument(0);
            assertThat(arg.getBalance()).isEqualTo(expectedBalance);
            return arg;
        });
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TransactionEntity result = transactionService.createTransaction(request, accountNumber, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualTo(depositAmount);
        assertThat(result.getType()).isEqualTo(TransactionType.deposit);
        verify(accountRepository).save(any(AccountEntity.class));
        verify(transactionRepository).save(any(TransactionEntity.class));
    }

    @Test
    void createTransaction_ShouldMaintainPrecision_WhenDepositAmountIsDecimal() {
        // Given
        BigDecimal depositAmount = BigDecimal.valueOf(123.45);
        CreateTransactionRequest request = new CreateTransactionRequest(123.45, gbp, TransactionType.deposit, "Bonus");
        BigDecimal expectedBalance = BigDecimal.valueOf(1123.45);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity arg = invocation.getArgument(0);
            assertThat(arg.getBalance()).isEqualByComparingTo(expectedBalance);
            return arg;
        });
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TransactionEntity result = transactionService.createTransaction(request, accountNumber, userId);

        // Then
        assertThat(result.getAmount()).isEqualByComparingTo(depositAmount);
    }

    @Test
    void createTransaction_ShouldWithdraw_WhenSufficientFundsAvailable() {
        // Given
        BigDecimal withdrawalAmount = BigDecimal.valueOf(300.00);
        CreateTransactionRequest request = new CreateTransactionRequest(300.00, gbp, TransactionType.withdrawal,
                "Withdrawal");
        BigDecimal expectedBalance = BigDecimal.valueOf(700.00);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity arg = invocation.getArgument(0);
            assertThat(arg.getBalance()).isEqualByComparingTo(expectedBalance);
            return arg;
        });
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TransactionEntity result = transactionService.createTransaction(request, accountNumber, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(withdrawalAmount);
        assertThat(result.getType()).isEqualTo(TransactionType.withdrawal);
    }

    @Test
    void createTransaction_ShouldThrowInsufficientFundsException_WhenFundsNotAvailable() {
        // Given
        CreateTransactionRequest request = new CreateTransactionRequest(1500.00, gbp, TransactionType.withdrawal,
                "Withdrawal");
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(request, accountNumber, userId))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds");

        verify(accountRepository, never()).save(any(AccountEntity.class));
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    void createTransaction_ShouldWithdraw_ExactlyToZeroBalance() {
        // Given
        CreateTransactionRequest request = new CreateTransactionRequest(1000.00, gbp, TransactionType.withdrawal,
                "Full withdrawal");
        BigDecimal expectedBalance = BigDecimal.ZERO;

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity arg = invocation.getArgument(0);
            assertThat(arg.getBalance()).isEqualByComparingTo(expectedBalance);
            return arg;
        });
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TransactionEntity result = transactionService.createTransaction(request, accountNumber, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
    }

    @Test
    void createTransaction_ShouldThrowInvalidTransactionException_WhenAmountIsZero() {
        // Given
        CreateTransactionRequest request = new CreateTransactionRequest(0.0, gbp, TransactionType.deposit, "Invalid");
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(request, accountNumber, userId))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("amount must be greater than zero");

        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    void createTransaction_ShouldThrowInvalidTransactionException_WhenAmountIsNegative() {
        // Given
        CreateTransactionRequest request = new CreateTransactionRequest(-100.0, gbp, TransactionType.deposit,
                "Invalid");
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(request, accountNumber, userId))
                .isInstanceOf(InvalidTransactionException.class)
                .hasMessageContaining("amount must be greater than zero");
    }

    @Test
    void createTransaction_ShouldThrowAccountNotFoundException_WhenAccountDoesNotExist() {
        // Given
        CreateTransactionRequest request = new CreateTransactionRequest(100.0, gbp, TransactionType.deposit, "Test");
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.createTransaction(request, accountNumber, userId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("Account not found");
    }

    @Test
    void createTransaction_ShouldMaintainPrecision_WithComplexArithmetic() {
        account.setBalance(BigDecimal.valueOf(10.99));
        CreateTransactionRequest request = new CreateTransactionRequest(10.98, gbp, TransactionType.withdrawal, "Test");
        BigDecimal expectedBalance = BigDecimal.valueOf(0.01);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity arg = invocation.getArgument(0);
            assertThat(arg.getBalance()).isEqualByComparingTo(expectedBalance);
            return arg;
        });
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TransactionEntity result = transactionService.createTransaction(request, accountNumber, userId);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    void createTransaction_ShouldHandleVerySmallAmounts() {
        // Given
        BigDecimal smallAmount = BigDecimal.valueOf(0.01);
        CreateTransactionRequest request = new CreateTransactionRequest(0.01, gbp, TransactionType.deposit, "Penny");
        BigDecimal expectedBalance = BigDecimal.valueOf(1000.01);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity arg = invocation.getArgument(0);
            assertThat(arg.getBalance()).isEqualByComparingTo(expectedBalance);
            return arg;
        });
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TransactionEntity result = transactionService.createTransaction(request, accountNumber, userId);

        // Then
        assertThat(result.getAmount()).isEqualByComparingTo(smallAmount);
    }

    @Test
    void createTransaction_ShouldHandleLargeAmounts() {
        // Given
        BigDecimal largeAmount = BigDecimal.valueOf(999999.99);
        CreateTransactionRequest request = new CreateTransactionRequest(999999.99, gbp, TransactionType.deposit,
                "Jackpot");
        BigDecimal expectedBalance = BigDecimal.valueOf(1000999.99);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(invocation -> {
            AccountEntity arg = invocation.getArgument(0);
            assertThat(arg.getBalance()).isEqualByComparingTo(expectedBalance);
            return arg;
        });
        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TransactionEntity result = transactionService.createTransaction(request, accountNumber, userId);

        // Then
        assertThat(result.getAmount()).isEqualByComparingTo(largeAmount);
    }

    private TransactionEntity createMockTransaction(String id, BigDecimal amount, TransactionType type) {
        return new TransactionEntity(amount, gbp, type, "Reference", account);
    }
}
