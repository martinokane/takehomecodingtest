package com.barclays.takehomecodingtest.repository;

import com.barclays.takehomecodingtest.model.AddressEmbeddable;
import com.barclays.takehomecodingtest.model.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        AddressEmbeddable address = new AddressEmbeddable("Line 1", "Line 2", "Line 3", "City", "County", "Postcode");

        user = new UserEntity();
        user.setUsername("testuser");
        user.setPassword("hashedPassword");
        user.setName("Test User");
        user.setAddress(address);
        user.setEmail("test@example.com");
        user.setPhone("+441234567890");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        entityManager.persist(user);
        entityManager.flush();
    }

    @Test
    void findByUsername_ShouldReturnUser_WhenUsernameExists() {
        // When
        Optional<UserEntity> found = userRepository.findByUsername("testuser");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getName()).isEqualTo("Test User");
    }

    @Test
    void findByUsername_ShouldReturnEmpty_WhenUsernameDoesNotExist() {
        // When
        Optional<UserEntity> found = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findById_ShouldReturnUser_WhenIdExists() {
        // When
        Optional<UserEntity> found = userRepository.findById(user.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(user.getId());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdDoesNotExist() {
        // When
        Optional<UserEntity> found = userRepository.findById("nonexistent");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenUsernameExists() {
        // When
        boolean exists = userRepository.existsByUsername("testuser");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenUsernameDoesNotExist() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }
}