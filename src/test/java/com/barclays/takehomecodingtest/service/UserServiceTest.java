package com.barclays.takehomecodingtest.service;

import com.barclays.takehomecodingtest.dto.Address;
import com.barclays.takehomecodingtest.dto.CreateUserRequest;
import com.barclays.takehomecodingtest.dto.UpdateUserRequest;
import com.barclays.takehomecodingtest.model.UserEntity;
import com.barclays.takehomecodingtest.repository.AccountRepository;
import com.barclays.takehomecodingtest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest createUserRequest;
    private UserEntity savedUser;

    @BeforeEach
    void setUp() {
        createUserRequest = new CreateUserRequest(
                "testuser",
                "password123",
                "Test User",
                new com.barclays.takehomecodingtest.dto.Address(
                        "123 Main St", "Apt 4B", "Line 3", "London", "Greater London", "SW1A 1AA"),
                "+441234567890",
                "test@example.com");

        savedUser = new UserEntity();
        savedUser.setUsername("testuser");
        savedUser.setPassword("hashedPassword");
        savedUser.setName("Test User");
    }

    @Test
    void createUser_ShouldCreateAndReturnUser() {
        // Given
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // When
        UserEntity result = userService.createUser(createUserRequest);

        // Then
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getPassword()).isEqualTo("hashedPassword");

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Given
        when(userRepository.findById("usr-1")).thenReturn(java.util.Optional.of(savedUser));

        // When
        UserEntity result = userService.getUserById("usr-1", "usr-1");

        // Then
        assertThat(result).isEqualTo(savedUser);
        verify(userRepository).findById("usr-1");
    }

    @Test
    void getUserById_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        // Given
        when(userRepository.findById("usr-1")).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById("usr-1", "usr-1"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: usr-1");

        verify(userRepository).findById("usr-1");
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenNoAccountsExist() {
        // Given
        when(userRepository.findById("usr-1")).thenReturn(java.util.Optional.of(savedUser));
        when(accountRepository.countByUserId("usr-1")).thenReturn(0L);

        // When
        userService.deleteUser("usr-1", "usr-1");

        // Then
        verify(userRepository).findById("usr-1");
        verify(accountRepository).countByUserId("usr-1");
        verify(userRepository).delete(savedUser);
    }

    @Test
    void deleteUser_ShouldThrowAccountAssociatedWithUserException_WhenAccountsExist() {
        // Given
        when(userRepository.findById("usr-1")).thenReturn(java.util.Optional.of(savedUser));
        when(accountRepository.countByUserId("usr-1")).thenReturn(1L);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser("usr-1", "usr-1"))
                .isInstanceOf(AccountAssociatedWithUserException.class)
                .hasMessage("Cannot delete user with id usr-1 because associated accounts exist");

        verify(userRepository).findById("usr-1");
        verify(accountRepository).countByUserId("usr-1");
        verify(userRepository, never()).delete(any(UserEntity.class));
    }

    @Test
    void deleteUser_ShouldThrowUserNotFoundException_WhenUserDoesNotExist() {
        // Given
        when(userRepository.findById("usr-1")).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser("usr-1", "usr-1"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: usr-1");

        verify(userRepository).findById("usr-1");
        verify(accountRepository, never()).countByUserId("usr-1");
        verify(userRepository, never()).delete(any(UserEntity.class));
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUser() {
        // Given
        Address newAddress = new Address("Line 1", "Line 2", "Line 3", "Manchester", "Greater Manchester", "M1 1AA");
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "newusername",
                "newpassword123",
                "New Name",
                newAddress, "+441234567891", "newemail@example.com");

        when(userRepository.findById("usr-1")).thenReturn(java.util.Optional.of(savedUser));
        when(userRepository.existsByUsername("newusername")).thenReturn(false);
        when(passwordEncoder.encode("newpassword123")).thenReturn("newHashedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // When
        UserEntity result = userService.updateUser("usr-1", updateRequest, "usr-1");

        // Then
        assertThat(result.getUsername()).isEqualTo("newusername");
        assertThat(result.getPassword()).isEqualTo("newHashedPassword");
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getPhoneNumber()).isEqualTo("+441234567891");
        assertThat(result.getAddress().getLine1()).isEqualTo(newAddress.line1());
        assertThat(result.getAddress().getLine2()).isEqualTo(newAddress.line2());
        assertThat(result.getAddress().getLine3()).isEqualTo(newAddress.line3());
        assertThat(result.getAddress().getTown()).isEqualTo(newAddress.town());
        assertThat(result.getAddress().getCounty()).isEqualTo(newAddress.county());
        assertThat(result.getAddress().getPostcode()).isEqualTo(newAddress.postcode());
        assertThat(result.getEmail()).isEqualTo("newemail@example.com");
        assertThat(result.getUpdatedAt()).isNotNull();
    }
}