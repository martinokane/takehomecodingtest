package com.barclays.takehomecodingtest.service;

import com.barclays.takehomecodingtest.dto.CreateUserRequest;
import com.barclays.takehomecodingtest.dto.UpdateUserRequest;
import com.barclays.takehomecodingtest.model.AddressEmbeddable;
import com.barclays.takehomecodingtest.model.UserEntity;
import com.barclays.takehomecodingtest.repository.AccountRepository;
import com.barclays.takehomecodingtest.repository.UserRepository;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, AccountRepository accountRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserEntity createUser(CreateUserRequest userData) {
        AddressEmbeddable address = new AddressEmbeddable(
                userData.address().line1(),
                userData.address().line2(),
                userData.address().line3(),
                userData.address().town(),
                userData.address().county(),
                userData.address().postcode());

        String hashedPassword = passwordEncoder.encode(userData.password());

        UserEntity userEntity = new UserEntity(
                userData.username(),
                hashedPassword,
                userData.name(),
                address,
                userData.phoneNumber(),
                userData.email());

        return userRepository.save(userEntity);
    }

    public UserEntity getUserById(String id, String authenticatedUserId) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        if (!authenticatedUserId.equals(id)) {
            logger.warn("User {} attempted to access user {} details", authenticatedUserId, id);
            throw new UnauthorizedAccessException("User is not authorized to access this resource");
        }
        return user;
    }

    public void deleteUser(String id, String authenticatedUserId) {
        UserEntity user = getUserById(id, authenticatedUserId);
        if (accountRepository.countByUserId(id) > 0) {
            logger.warn("Attempted to delete user with id {} but associated accounts exist. Returning conflict.", id);
            throw new AccountAssociatedWithUserException(
                    "Cannot delete user with id " + id + " because associated accounts exist");
        }
        userRepository.delete(user);
    }

    public UserEntity updateUser(String id, UpdateUserRequest updateRequest, String authenticatedUserId) {
        UserEntity user = getUserById(id, authenticatedUserId);

        if (updateRequest.username() != null && !updateRequest.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(updateRequest.username())) {
                logger.warn("Attempted to update user {} with username {} that is already taken", id,
                        updateRequest.username());
                throw new UserAlreadyExistsException("Username " + updateRequest.username() + " is already taken");
            }
            user.setUsername(updateRequest.username());
        }

        // Update other fields if provided
        if (updateRequest.password() != null) {
            user.setPassword(passwordEncoder.encode(updateRequest.password()));
        }
        if (updateRequest.name() != null) {
            user.setName(updateRequest.name());
        }
        if (updateRequest.phoneNumber() != null) {
            user.setPhone(updateRequest.phoneNumber());
        }
        if (updateRequest.email() != null) {
            user.setEmail(updateRequest.email());
        }
        if (updateRequest.address() != null) {
            AddressEmbeddable address = new AddressEmbeddable(
                    updateRequest.address().line1(),
                    updateRequest.address().line2(),
                    updateRequest.address().line3(),
                    updateRequest.address().town(),
                    updateRequest.address().county(),
                    updateRequest.address().postcode());
            user.setAddress(address);
        }

        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}
