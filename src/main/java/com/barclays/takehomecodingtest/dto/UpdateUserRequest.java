package com.barclays.takehomecodingtest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
        @Pattern(regexp = ".*\\S.*", message = "must not be blank") String username,
        @Pattern(regexp = ".*\\S.*", message = "must not be blank") String password,
        @Pattern(regexp = ".*\\S.*", message = "must not be blank") String name,
        @Valid Address address,
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$") String phoneNumber,
        @Email @Pattern(regexp = ".*\\S.*", message = "must not be blank") String email) {
}