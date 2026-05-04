package com.barclays.takehomecodingtest.controller;

import com.barclays.takehomecodingtest.controllers.UserController;
import com.barclays.takehomecodingtest.dto.Address;
import com.barclays.takehomecodingtest.dto.CreateUserRequest;
import com.barclays.takehomecodingtest.model.AddressEmbeddable;
import com.barclays.takehomecodingtest.model.UserEntity;
import com.barclays.takehomecodingtest.security.JwtTokenProvider;
import com.barclays.takehomecodingtest.service.AccountAssociatedWithUserException;
import com.barclays.takehomecodingtest.service.UnauthorizedAccessException;
import com.barclays.takehomecodingtest.service.UserNotFoundException;
import com.barclays.takehomecodingtest.service.UserService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = { "api.version=v1" })
class UserControllerTest {

        @Mock
        private UserService userService;

        @Mock
        private JwtTokenProvider jwtTokenProvider;

        @Autowired
        private WebApplicationContext webApplicationContext;

        private MockMvc mockMvc;
        private ObjectMapper objectMapper;
        private CreateUserRequest createUserRequest;
        private UserEntity user;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                .build();
                ReflectionTestUtils.setField(
                                webApplicationContext.getBean(
                                                UserController.class),
                                "userService", userService);
                ReflectionTestUtils.setField(
                                webApplicationContext.getBean(
                                                UserController.class),
                                "jwtTokenProvider", jwtTokenProvider);
                objectMapper = new ObjectMapper();

                createUserRequest = new CreateUserRequest(
                                "testuser",
                                "password123",
                                "Test User",
                                new Address(
                                                "123 Main St", "Line 2", "Line 3", "London", "County",
                                                "SW1A 1AA"),
                                "+441234567890",
                                "test@example.com");

                user = new UserEntity();
                user.setId("usr-1");
                user.setUsername("testuser");
                user.setName("Test User");
                user.setAddress(new AddressEmbeddable(
                                "123 Main St", "Line 2", "Line 3", "London", "County",
                                "SW1A 1AA"));
                user.setPhone("+441234567890");
                user.setEmail("test@test.com");
        }

        @Test
        void createUser_ShouldReturnCreated_WhenValidRequest() throws Exception {
                // Given
                when(userService.createUser(any(CreateUserRequest.class))).thenReturn(user);

                // When & Then
                mockMvc.perform(post("/v1/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createUserRequest)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value("usr-1"))
                                .andExpect(jsonPath("$.username").value("testuser"));
        }

        @Test
        void getUserById_ShouldReturnUser_WhenUserExistsAndAuthenticated() throws Exception {
                // Given
                when(jwtTokenProvider.validateToken(any())).thenReturn(true);
                when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn("usr-1");
                when(userService.getUserById("usr-1", "usr-1")).thenReturn(user);

                // When & Then
                mockMvc.perform(get("/v1/users/usr-1")
                                .header("Authorization", "Bearer valid-token"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value("usr-1"))
                                .andExpect(jsonPath("$.username").value("testuser"));
        }

        @Test
        void getUserById_ShouldReturnUnauthorized_WhenNoAuthHeader() throws Exception {
                // When & Then
                mockMvc.perform(get("/v1/users/usr-1"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void getUserById_ShouldReturnForbidden_WhenAccessingOtherUser() throws Exception {
                // Given
                when(jwtTokenProvider.validateToken(any())).thenReturn(true);
                when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn("usr-2");
                when(userService.getUserById("usr-1", "usr-2"))
                                .thenThrow(new UnauthorizedAccessException("User not found"));

                // When & Then
                mockMvc.perform(get("/v1/users/usr-1")
                                .header("Authorization", "Bearer valid-token"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void getUserById_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
                // Given
                when(jwtTokenProvider.validateToken(any())).thenReturn(true);
                when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn("usr-1");
                when(userService.getUserById("usr-1", "usr-1")).thenThrow(new UserNotFoundException("User not found"));

                // When & Then
                mockMvc.perform(get("/v1/users/usr-1")
                                .header("Authorization", "Bearer valid-token"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void deleteUser_ShouldReturnNoContent_WhenUserDeletedSuccessfully() throws Exception {
                // Given
                when(jwtTokenProvider.validateToken(any())).thenReturn(true);
                when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn("usr-1");

                // When & Then
                mockMvc.perform(delete("/v1/users/usr-1")
                                .header("Authorization", "Bearer valid-token"))
                                .andExpect(status().isNoContent());
        }

        @Test
        void deleteUser_ShouldReturnForbidden_WhenDeletingOtherUser() throws Exception {
                // Given
                when(jwtTokenProvider.validateToken(any())).thenReturn(true);
                when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn("usr-2");
                doThrow(new UnauthorizedAccessException(null)).when(userService).deleteUser("usr-1", "usr-2");

                // When & Then
                mockMvc.perform(delete("/v1/users/usr-1")
                                .header("Authorization", "Bearer valid-token"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void deleteUser_ShouldReturnConflict_WhenUserHasAccounts() throws Exception {
                // Given
                when(jwtTokenProvider.validateToken(any())).thenReturn(true);
                when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn("usr-1");
                doThrow(new AccountAssociatedWithUserException("Cannot delete user with accounts"))
                                .when(userService).deleteUser("usr-1", "usr-1");

                // When & Then
                mockMvc.perform(delete("/v1/users/usr-1")
                                .header("Authorization", "Bearer valid-token"))
                                .andExpect(status().isConflict())
                                .andExpect(content().json("{\"message\":\"Cannot delete user with accounts\"}"));
        }

        @Test
        void deleteUser_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
                // Given
                when(jwtTokenProvider.validateToken(any())).thenReturn(true);
                when(jwtTokenProvider.getUserIdFromToken(any())).thenReturn("usr-1");
                doThrow(new UserNotFoundException("User not found")).when(userService).deleteUser("usr-1", "usr-1");

                // When & Then
                mockMvc.perform(delete("/v1/users/usr-1")
                                .header("Authorization", "Bearer valid-token"))
                                .andExpect(status().isNotFound());
        }
}