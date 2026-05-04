package com.barclays.takehomecodingtest.dto;

public record AuthResponse(
                String token,
                String userId,
                String username) {
}
