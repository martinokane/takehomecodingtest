package com.barclays.takehomecodingtest.controller;

import com.barclays.takehomecodingtest.dto.AuthResponse;
import com.barclays.takehomecodingtest.dto.LoginRequest;
import com.barclays.takehomecodingtest.service.AuthService;
import com.barclays.takehomecodingtest.service.AuthenticationFailedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = { "api.version=v1" })
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
        // Replace the real AuthService with the mock
        org.springframework.test.util.ReflectionTestUtils.setField(
                webApplicationContext.getBean(com.barclays.takehomecodingtest.controllers.AuthController.class),
                "authService", authService);

        objectMapper = new ObjectMapper();

        loginRequest = new LoginRequest("testuser", "password123");
        authResponse = new AuthResponse("jwt-token", "usr-1", "testuser");
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() throws Exception {
        // Given
        when(authService.authenticate(any(LoginRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.userId").value("usr-1"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenCredentialsAreInvalid() throws Exception {
        // Given
        when(authService.authenticate(any(LoginRequest.class)))
                .thenThrow(new AuthenticationFailedException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_ShouldReturnBadRequest_WhenRequestIsInvalid() throws Exception {
        // Given - Invalid request (empty username)
        LoginRequest invalidRequest = new LoginRequest("", "password123");

        // When & Then
        mockMvc.perform(post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}