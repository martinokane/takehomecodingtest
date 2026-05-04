package com.barclays.takehomecodingtest.repository;

import com.barclays.takehomecodingtest.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findById(String id);

    boolean existsByUsername(String username);
}
