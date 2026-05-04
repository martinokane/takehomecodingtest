package com.barclays.takehomecodingtest.repository;

import com.barclays.takehomecodingtest.dto.AccountType;
import com.barclays.takehomecodingtest.dto.SortCode;
import com.barclays.takehomecodingtest.dto.TransactionType;
import com.barclays.takehomecodingtest.model.AccountEntity;
import com.barclays.takehomecodingtest.model.AddressEmbeddable;
import com.barclays.takehomecodingtest.model.TransactionEntity;
import com.barclays.takehomecodingtest.model.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private UserEntity user;

    private AccountEntity account;

    @BeforeEach
    void setUp() {
        AddressEmbeddable address = new AddressEmbeddable("Line 1", "Line 2", "Line 3", "City", "County", "Postcode");

        user = new UserEntity();
        user.setUsername("testuser");
        user.setPassword("hashedPassword");
        user.setName("Test User");
        user.setAddress(address);
        user.setEmail("test@example.com");
        user.setPhone("+441234567890");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(user);

        account = new AccountEntity();
        account.setName("Savings Account");
        account.setAccountType(AccountType.PERSONAL);
        account.setUser(user);
        account.setSortCode(SortCode.DEFAULT);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(account);

        TransactionEntity transaction = new TransactionEntity();
        transaction.setAccount(account);
        transaction.setUser(user);
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setType(TransactionType.deposit);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setCurrency(Currency.getInstance("GBP"));

        entityManager.persist(transaction);
        entityManager.flush();
    }

    @Test
    void findByAccountAccountNumber_ShouldReturnTransactionsWithSameAccountNumber() {
        // When
        List<TransactionEntity> found = transactionRepository.findByAccountAccountNumber(account.getAccountNumber());

        // Then
        assertThat(found).isNotEmpty();
        assertThat(found.get(0).getAccount().getAccountNumber()).isEqualTo(account.getAccountNumber());
    }

    @Test
    void findByAccountAccountNumber_ShouldReturnEmpty_WhenAccountNumberDoesNotExist() {
        // When
        List<TransactionEntity> found = transactionRepository.findByAccountAccountNumber("NONEXISTENT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByAccountAccountNumber_ShouldReturnMultipleTransactions_WhenMultipleExist() {
        // Given - create additional transaction
        TransactionEntity anotherTransaction = new TransactionEntity(
                BigDecimal.valueOf(50),
                Currency.getInstance("GBP"),
                TransactionType.withdrawal,
                "Withdrawal",
                account);
        entityManager.persist(anotherTransaction);
        entityManager.flush();

        // When
        List<TransactionEntity> found = transactionRepository.findByAccountAccountNumber(account.getAccountNumber());

        // Then
        assertThat(found).hasSize(2);
        assertThat(found).allMatch(t -> t.getAccount().getAccountNumber().equals(account.getAccountNumber()));
    }

    @Test
    void save_ShouldGenerateTransactionId_WithCorrectFormat() {
        // Given
        TransactionEntity transaction = new TransactionEntity(
                BigDecimal.valueOf(75),
                Currency.getInstance("GBP"),
                TransactionType.deposit,
                "Test",
                account);

        // When
        TransactionEntity saved = transactionRepository.save(transaction);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).matches("^tan-[A-Za-z0-9]{8}$");
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUsernameDoesNotExist() {
        // When
        Optional<UserEntity> found = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }
}