package com.barclays.takehomecodingtest.repository;

import com.barclays.takehomecodingtest.dto.AccountType;
import com.barclays.takehomecodingtest.dto.SortCode;
import com.barclays.takehomecodingtest.model.AccountEntity;
import com.barclays.takehomecodingtest.model.AddressEmbeddable;
import com.barclays.takehomecodingtest.model.UserEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    private UserEntity user;
    private AccountEntity account1;
    private AccountEntity account2;

    @BeforeEach
    void setUp() {
        AddressEmbeddable address = new AddressEmbeddable("Line 1", "Line 2", "Line 3", "City", "County", "Postcode");

        user = new UserEntity();
        user.setUsername("testuser");
        user.setPassword("hashedPassword");
        user.setName("Test User");
        user.setAddress(address);
        user.setEmail("test@test.com");
        user.setPhone("+441234567890");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(user);

        account1 = new AccountEntity();
        account1.setName("Savings Account");
        account1.setAccountType(AccountType.PERSONAL);
        account1.setUser(user);
        account1.setSortCode(SortCode.DEFAULT);
        account1.setCreatedAt(LocalDateTime.now());
        account1.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(account1);

        account2 = new AccountEntity();
        account2.setName("Business Account");
        account2.setAccountType(AccountType.PERSONAL);
        account2.setUser(user);
        account2.setSortCode(SortCode.DEFAULT);
        account2.setCreatedAt(LocalDateTime.now());
        account2.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(account2);

        entityManager.flush();
    }

    @Test
    void findByUserId_ShouldReturnAllAccountsForUser() {
        // When
        List<AccountEntity> accounts = accountRepository.findByUserId(user.getId());

        // Then
        assertThat(accounts).hasSize(2);
        assertThat(accounts).extracting(AccountEntity::getName)
                .containsExactlyInAnyOrder("Savings Account", "Business Account");
    }

    @Test
    void findByUserId_ShouldReturnEmptyList_WhenUserHasNoAccounts() {
        // Given
        AddressEmbeddable address = new AddressEmbeddable("Line 1", "Line 2", "Line 3", "City", "County", "Postcode");
        UserEntity otherUser = new UserEntity();
        otherUser.setUsername("otheruser");
        otherUser.setPassword("hashedPassword");
        otherUser.setName("Other User");
        otherUser.setAddress(address);
        otherUser.setEmail("test@test.com");
        otherUser.setPhone("+441234567890");
        otherUser.setCreatedAt(LocalDateTime.now());
        otherUser.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(otherUser);
        entityManager.flush();

        // When
        List<AccountEntity> accounts = accountRepository.findByUserId(otherUser.getId());

        // Then
        assertThat(accounts).isEmpty();
    }

    @Test
    void countByUserId_ShouldReturnCountOfAccountsForUser() {
        // When
        long count = accountRepository.countByUserId(user.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void countByUserId_ShouldReturnZero_WhenUserHasNoAccounts() {
        // Given
        AddressEmbeddable address = new AddressEmbeddable("Line 1", "Line 2", "Line 3", "City", "County", "Postcode");
        UserEntity otherUser = new UserEntity();
        otherUser.setUsername("otheruser");
        otherUser.setPassword("hashedPassword");
        otherUser.setName("Other User");
        otherUser.setAddress(address);
        otherUser.setEmail("test@test.com");
        otherUser.setPhone("+441234567890");
        otherUser.setCreatedAt(LocalDateTime.now());
        otherUser.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(otherUser);
        entityManager.flush();

        // When
        long count = accountRepository.countByUserId(otherUser.getId());

        // Then
        assertThat(count).isEqualTo(0);
    }

    @Test
    void findByAccountNumber_ShouldReturnAccount_WhenAccountExists() {
        // When
        Optional<AccountEntity> account = accountRepository.findByAccountNumber(account1.getAccountNumber());

        // Then
        assertThat(account).isPresent();
        assertThat(account.get().getAccountNumber()).isEqualTo(account1.getAccountNumber());
    }

    @Test
    void findByAccountNumber_ShouldReturnEmptyOptional_WhenAccountDoesNotExist() {
        // When
        Optional<AccountEntity> account = accountRepository.findByAccountNumber("nonexistent");

        // Then
        assertThat(account).isNotPresent();
    }
}