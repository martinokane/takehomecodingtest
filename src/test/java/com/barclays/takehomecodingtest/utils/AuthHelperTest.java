package com.barclays.takehomecodingtest.utils;

import com.barclays.takehomecodingtest.security.JwtTokenProvider;
import com.barclays.takehomecodingtest.service.AuthenticationFailedException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthHelperTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private String validToken;

    @BeforeEach
    void setUp() {
        validToken = "Bearer valid-jwt-token";
    }

    @Test
    void getAuthenticatedUserIdFromRequest_ShouldReturnUserId_WhenTokenIsValid() {
        // Given
        when(request.getHeader("Authorization")).thenReturn(validToken);
        when(jwtTokenProvider.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("valid-jwt-token")).thenReturn("usr-1");

        // When
        String userId = AuthHelper.getAuthenticatedUserIdFromRequest(request, jwtTokenProvider);

        // Then
        assertThat(userId).isEqualTo("usr-1");
    }

    @Test
    void getAuthenticatedUserIdFromRequest_ShouldThrowException_WhenAuthHeaderIsNull() {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> AuthHelper.getAuthenticatedUserIdFromRequest(request, jwtTokenProvider))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid or missing JWT token");
    }

    @Test
    void getAuthenticatedUserIdFromRequest_ShouldThrowException_WhenTokenIsInvalid() {
        // Given
        when(request.getHeader("Authorization")).thenReturn(validToken);
        when(jwtTokenProvider.validateToken("valid-jwt-token")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> AuthHelper.getAuthenticatedUserIdFromRequest(request, jwtTokenProvider))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid or missing JWT token");
    }

    @Test
    void getAuthenticatedUserIdFromRequest_ShouldThrowException_WhenTokenExtractionFails() {
        // Given
        when(request.getHeader("Authorization")).thenReturn(validToken);
        when(jwtTokenProvider.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken("valid-jwt-token"))
                .thenThrow(new RuntimeException("Token parsing error"));

        // When & Then
        assertThatThrownBy(() -> AuthHelper.getAuthenticatedUserIdFromRequest(request, jwtTokenProvider))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Failed to extract user ID from token: Token parsing error");
    }
}