package com.barclays.takehomecodingtest.controller;

import com.barclays.takehomecodingtest.dto.AccountType;
import com.barclays.takehomecodingtest.dto.CreateAccountRequest;
import com.barclays.takehomecodingtest.model.AccountEntity;
import com.barclays.takehomecodingtest.model.UserEntity;
import com.barclays.takehomecodingtest.security.JwtTokenProvider;
import com.barclays.takehomecodingtest.service.AccountService;
import com.barclays.takehomecodingtest.service.UnauthorizedAccessException;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import com.barclays.takehomecodingtest.controllers.AccountController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = { "api.version=v1" })
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private CreateAccountRequest createAccountRequest;
    private AccountEntity account;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
        // Replace the real services with mocks
        ReflectionTestUtils.setField(
                webApplicationContext.getBean(AccountController.class),
                "accountService", accountService);
        ReflectionTestUtils.setField(
                webApplicationContext.getBean(AccountController.class),
                "jwtTokenProvider", jwtTokenProvider);

        objectMapper = new ObjectMapper();

        createAccountRequest = new CreateAccountRequest("Savings Account", AccountType.PERSONAL);

        user = new UserEntity();
        user.setUsername("testuser");
        user.setId("usr-1");

        account = new AccountEntity();
        account.setName("Savings Account");
        account.setAccountType(AccountType.PERSONAL);
        account.setUser(user);
        account.setSortCode(com.barclays.takehomecodingtest.dto.SortCode.DEFAULT);
        account.setAccountNumber("01000000");
    }

    @Test
    void createAccount_ShouldReturnAccount_WhenValidRequest() throws Exception {
        // Given
        when(jwtTokenProvider.validateToken(any())).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn("usr-1");
        when(accountService.createAccount(any(CreateAccountRequest.class), any(String.class))).thenReturn(account);

        // When & Then
        mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccountRequest))
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("01000000"))
                .andExpect(jsonPath("$.name").value("Savings Account"))
                .andExpect(jsonPath("$.accountType").value("PERSONAL"));
    }

    @Test
    void createAccount_ShouldReturnUnauthorized_WhenNoAuthHeader() throws Exception {
        // When & Then
        mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createAccountRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAccount_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given - Invalid request (empty name)
        CreateAccountRequest invalidRequest = new CreateAccountRequest("", AccountType.PERSONAL);

        // When & Then
        mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAccount_ShouldReturnAccount_WhenAccountExistsAndBelongsToUser() throws Exception {
        // Given
        when(jwtTokenProvider.validateToken(any())).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn("usr-1");
        when(accountService.getAccountByAccountNumber("10000000", "usr-1")).thenReturn(account);

        // When & Then
        mockMvc.perform(get("/v1/accounts/10000000")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("01000000"))
                .andExpect(jsonPath("$.name").value("Savings Account"))
                .andExpect(jsonPath("$.accountType").value("PERSONAL"));
    }

    @Test
    void getAccount_ShouldReturnForbidden_WhenAccountExistsButDoesNotBelongToUser() throws Exception {
        // Given
        when(jwtTokenProvider.validateToken(any())).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn("usr-2");
        when(accountService.getAccountByAccountNumber("10000000", "usr-2"))
                .thenThrow(new UnauthorizedAccessException(null));

        // When & Then
        mockMvc.perform(get("/v1/accounts/10000000")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAccount_ShouldReturnNotFound_WhenAccountDoesNotExist() throws Exception {
        // Given
        when(jwtTokenProvider.validateToken(any())).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn("usr-1");
        when(accountService.getAccountByAccountNumber("99999999", "usr-1"))
                .thenThrow(new com.barclays.takehomecodingtest.service.AccountNotFoundException(
                        "Account not found with account number: 99999999"));

        // When & Then
        mockMvc.perform(get("/v1/accounts/99999999")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listAccounts_ShouldReturnListOfAccountsForUser() throws Exception {
        // Given
        when(jwtTokenProvider.validateToken(any())).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn("usr-1");
        when(accountService.listAccounts("usr-1")).thenReturn(List.of(account));

        // When & Then
        mockMvc.perform(get("/v1/accounts")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts").isArray())
                .andExpect(jsonPath("$.accounts[0].accountNumber").value("01000000"))
                .andExpect(jsonPath("$.accounts[0].name").value("Savings Account"))
                .andExpect(jsonPath("$.accounts[0].accountType").value("PERSONAL"));
    }
}