package com.barclays.takehomecodingtest.controller;

import com.barclays.takehomecodingtest.controllers.TransactionController;
import com.barclays.takehomecodingtest.dto.AccountType;
import com.barclays.takehomecodingtest.dto.CreateTransactionRequest;
import com.barclays.takehomecodingtest.dto.SortCode;
import com.barclays.takehomecodingtest.dto.TransactionType;
import com.barclays.takehomecodingtest.model.AccountEntity;
import com.barclays.takehomecodingtest.model.TransactionEntity;
import com.barclays.takehomecodingtest.model.UserEntity;
import com.barclays.takehomecodingtest.security.JwtTokenProvider;
import com.barclays.takehomecodingtest.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = { "api.version=v1" })
class TransactionControllerTest {

        @Mock
        private TransactionService transactionService;

        @Mock
        private JwtTokenProvider jwtTokenProvider;

        @Autowired
        private WebApplicationContext webApplicationContext;

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;
        private AccountEntity account;
        private UserEntity user;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .build();
                // Replace the real services with mocks
                ReflectionTestUtils.setField(
                                webApplicationContext.getBean(
                                                TransactionController.class),
                                "transactionService", transactionService);
                ReflectionTestUtils.setField(
                                webApplicationContext.getBean(
                                                TransactionController.class),
                                "jwtTokenProvider", jwtTokenProvider);

                objectMapper = new ObjectMapper();

                user = new UserEntity();
                user.setUsername("testuser");
                user.setId("usr-1");

                account = new AccountEntity();
                account.setName("Savings Account");
                account.setAccountType(AccountType.PERSONAL);
                account.setUser(user);
                account.setSortCode(SortCode.DEFAULT);
                account.setAccountNumber("01000000");
        }

        @Test
        void listTransactions_ShouldReturnListOfTransactionsForAccount() throws Exception {
                // Given
                when(jwtTokenProvider.validateToken(any())).thenReturn(true);
                when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn("usr-1");
                when(transactionService.listTransactions("01000000", "usr-1")).thenReturn(List.of());

                // When & Then
                mockMvc.perform(get("/v1/accounts/01000000/transactions")
                                .header("Authorization", "Bearer valid-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.transactions").isArray());
        }

        @Test
        void createTransaction_ShouldReturnCreatedTransaction() throws Exception {
                // Given
                when(jwtTokenProvider.validateToken(any())).thenReturn(true);
                when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn("usr-1");
                when(transactionService.createTransaction(any(), any(), any()))
                                .thenReturn(new TransactionEntity(BigDecimal.valueOf(100.0),
                                                Currency.getInstance("GBP"), TransactionType.withdrawal, "ref",
                                                account));
                CreateTransactionRequest createTransactionRequest = new CreateTransactionRequest(100.0,
                                Currency.getInstance("GBP"), TransactionType.withdrawal, "ref");

                // When & Then
                mockMvc.perform(post("/v1/accounts/01000000/transactions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createTransactionRequest))
                                .header("Authorization", "Bearer valid-token"))
                                .andExpect(status().isCreated());
        }
}