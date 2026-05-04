package com.barclays.takehomecodingtest.controllers;

import com.barclays.takehomecodingtest.dto.CreateUserRequest;
import com.barclays.takehomecodingtest.dto.UpdateUserRequest;
import com.barclays.takehomecodingtest.dto.UserResponse;
import com.barclays.takehomecodingtest.model.UserEntity;
import com.barclays.takehomecodingtest.security.JwtTokenProvider;
import com.barclays.takehomecodingtest.service.UserService;
import com.barclays.takehomecodingtest.utils.AuthHelper;
import com.barclays.takehomecodingtest.utils.UserHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@Tag(name = "user", description = "Manage a user")
@RequestMapping("/${api.version}/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping()
    @Operation(summary = "Create user", description = "Creates a new user account. User does not need to be authenticated to create an account.")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest userData) {
        logger.info("Creating user with data: {}", userData);
        UserEntity savedUser = userService.createUser(userData);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserHelper.convertUserEntityToUserResponse(savedUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user", description = "Retrieves the details of a specific user by ID for the authenticated user.")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id, HttpServletRequest request) {
        logger.info("Fetching user with id: {}", id);
        String authenticatedUserId = AuthHelper.getAuthenticatedUserIdFromRequest(request, jwtTokenProvider);
        UserEntity user = userService.getUserById(id, authenticatedUserId);
        return ResponseEntity.ok(UserHelper.convertUserEntityToUserResponse(user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a specific user by ID for the authenticated user.")
    public ResponseEntity<String> deleteUser(@PathVariable String id, HttpServletRequest request) {
        logger.info("Deleting user with id: {}", id);
        String authenticatedUserId = AuthHelper.getAuthenticatedUserIdFromRequest(request, jwtTokenProvider);
        userService.deleteUser(id, authenticatedUserId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates the details of a specific user by ID for the authenticated user.")
    public ResponseEntity<Object> updateUser(@PathVariable String id,
            @Valid @RequestBody UpdateUserRequest updateRequest,
            HttpServletRequest request) {
        logger.info("Updating user with id: {}", id);
        String authenticatedUserId = AuthHelper.getAuthenticatedUserIdFromRequest(request, jwtTokenProvider);
        UserEntity updatedUser = userService.updateUser(id, updateRequest, authenticatedUserId);
        return ResponseEntity.ok(UserHelper.convertUserEntityToUserResponse(updatedUser));
    }
}