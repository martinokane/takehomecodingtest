package com.barclays.takehomecodingtest.utils;

import com.barclays.takehomecodingtest.security.JwtTokenProvider;
import com.barclays.takehomecodingtest.service.AuthenticationFailedException;

import jakarta.servlet.http.HttpServletRequest;

public class AuthHelper {
    public static String getAuthenticatedUserIdFromRequest(HttpServletRequest request,
            JwtTokenProvider jwtTokenProvider) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthenticationFailedException("Invalid or missing JWT token");
        }

        String token = authHeader.substring(7).trim();
        if (token.isEmpty() || !jwtTokenProvider.validateToken(token)) {
            throw new AuthenticationFailedException("Invalid or missing JWT token");
        }

        try {
            return jwtTokenProvider.getUserIdFromToken(token);
        } catch (Exception e) {
            throw new AuthenticationFailedException("Failed to extract user ID from token: " + e.getMessage());
        }
    }
}
