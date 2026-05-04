package com.barclays.takehomecodingtest.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();

        secretKey = Keys.hmacShaKeyFor("12345678901234567890123456789012".getBytes());

        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "12345678901234567890123456789012");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L); // 1 hour
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // Given
        String userId = "usr-1";
        String username = "testuser";

        // When
        String token = jwtTokenProvider.generateToken(userId, username);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        assertThat(claims.getSubject()).isEqualTo(username);
        assertThat(claims.get("userId", String.class)).isEqualTo(userId);
    }

    @Test
    void getUserIdFromToken_ShouldReturnUserId_WhenTokenIsValid() {
        // Given
        String token = jwtTokenProvider.generateToken("usr-1", "testuser");

        // When
        String userId = jwtTokenProvider.getUserIdFromToken(token);

        // Then
        assertThat(userId).isEqualTo("usr-1");
    }

    @Test
    void getUserIdFromToken_ShouldThrowException_WhenTokenIsInvalid() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThatThrownBy(() -> jwtTokenProvider.getUserIdFromToken(invalidToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getUsernameFromToken_ShouldReturnUsername_WhenTokenIsValid() {
        // Given
        String token = jwtTokenProvider.generateToken("usr-1", "testuser");

        // When
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void getUsernameFromToken_ShouldThrowException_WhenTokenIsInvalid() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThatThrownBy(() -> jwtTokenProvider.getUsernameFromToken(invalidToken))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        // Given
        String token = jwtTokenProvider.generateToken("usr-1", "testuser");

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsExpired() {
        // Given - Create expired token
        Date pastDate = new Date(System.currentTimeMillis() - 7200000L); // 2 hours ago
        String expiredToken = Jwts.builder()
                .subject("usr-1")
                .claim("username", "testuser")
                .issuedAt(pastDate)
                .expiration(pastDate)
                .signWith(secretKey)
                .compact();

        // When
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsMalformed() {
        // Given
        String malformedToken = "malformed.token";

        // When
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }
}