package com.barclays.takehomecodingtest.dto;

import java.time.LocalDateTime;

public record UserResponse(
                String id,
                String username,
                String name,
                String phoneNumber,
                String email,
                Address address,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
}
