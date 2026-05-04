package com.barclays.takehomecodingtest.service;

import com.barclays.takehomecodingtest.dto.AuthResponse;
import com.barclays.takehomecodingtest.dto.LoginRequest;
import com.barclays.takehomecodingtest.model.UserEntity;
import com.barclays.takehomecodingtest.repository.UserRepository;
import com.barclays.takehomecodingtest.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("testuser", "password123");

        user = new UserEntity();
        user.setUsername("testuser");
        user.setPassword("hashedPassword");
        user.setId("usr-1");
    }

    @Test
    void authenticate_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken("usr-1", "testuser")).thenReturn("jwt-token");

        // When
        AuthResponse result = authService.authenticate(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.username()).isEqualTo("testuser");

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "hashedPassword");
        verify(jwtTokenProvider).generateToken("usr-1", "testuser");
    }

    @Test
    void authenticate_ShouldThrowAuthenticationFailedException_WhenUsernameDoesNotExist() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.authenticate(loginRequest))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid username or password");

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, org.mockito.Mockito.never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, org.mockito.Mockito.never()).generateToken(anyString(), anyString());
    }

    @Test
    void authenticate_ShouldThrowAuthenticationFailedException_WhenPasswordIsIncorrect() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.authenticate(loginRequest))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid username or password");

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", "hashedPassword");
        verify(jwtTokenProvider, org.mockito.Mockito.never()).generateToken(anyString(), anyString());
    }
}