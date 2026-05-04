package com.barclays.takehomecodingtest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
                @NotBlank String username,
                @NotBlank String password,
                @NotBlank String name,
                @NotNull @Valid Address address,
                @NotBlank @Pattern(regexp = "^\\+[1-9]\\d{1,14}$") String phoneNumber,
                @NotBlank @Email String email) {
}
