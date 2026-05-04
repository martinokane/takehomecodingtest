package com.barclays.takehomecodingtest.service;

import com.barclays.takehomecodingtest.dto.AccountType;
import com.barclays.takehomecodingtest.dto.CreateAccountRequest;
import com.barclays.takehomecodingtest.model.AccountEntity;
import com.barclays.takehomecodingtest.model.UserEntity;
import com.barclays.takehomecodingtest.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AccountService accountService;

    private CreateAccountRequest createAccountRequest;
    private UserEntity user;
    private AccountEntity savedAccount;

    @BeforeEach
    void setUp() {
        createAccountRequest = new CreateAccountRequest("Savings Account", AccountType.PERSONAL);

        user = new UserEntity();
        user.setUsername("testuser");
        user.setId("usr-1");

        savedAccount = new AccountEntity();
        savedAccount.setName("Savings Account");
        savedAccount.setAccountType(AccountType.PERSONAL);
        savedAccount.setUser(user);
        savedAccount.setAccountNumber("01000000");
    }

    @Test
    void createAccount_ShouldCreateAndReturnAccount() {
        // Given
        when(userService.getUserById("usr-1", "usr-1")).thenReturn(user);
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(savedAccount);

        // When
        AccountEntity result = accountService.createAccount(createAccountRequest, "usr-1");

        // Then
        assertThat(result.getAccountNumber()).isEqualTo("01000000");
        assertThat(result.getName()).isEqualTo("Savings Account");
        assertThat(result.getAccountType()).isEqualTo(AccountType.PERSONAL);
        assertThat(result.getUser()).isEqualTo(user);

        verify(userService).getUserById("usr-1", "usr-1");
        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    void listAccounts_ShouldReturnListOfAccountsForUser() {
        // Given
        when(accountRepository.findByUserId("usr-1")).thenReturn(List.of(savedAccount));

        // When
        var accounts = accountService.listAccounts("usr-1");

        // Then
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0)).isEqualTo(savedAccount);

        verify(accountRepository).findByUserId("usr-1");
    }

    @Test
    void getAccountByAccountNumber_ShouldReturnAccount_WhenAccountExists() {
        // Given
        when(accountRepository.findByAccountNumber("01000000")).thenReturn(java.util.Optional.of(savedAccount));

        // When
        AccountEntity result = accountService.getAccountByAccountNumber("01000000", "usr-1");

        // Then
        assertThat(result).isEqualTo(savedAccount);

        verify(accountRepository).findByAccountNumber("01000000");
    }

    @Test
    void getAccountByAccountNumber_ShouldThrowAccountNotFoundException_WhenAccountDoesNotExist() {
        // Given
        when(accountRepository.findByAccountNumber("99999999")).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accountService.getAccountByAccountNumber("99999999", "usr-1"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account not found with account number: 99999999");

        verify(accountRepository).findByAccountNumber("99999999");
    }
}