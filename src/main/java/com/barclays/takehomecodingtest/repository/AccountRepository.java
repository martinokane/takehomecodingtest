package com.barclays.takehomecodingtest.repository;

import com.barclays.takehomecodingtest.model.AccountEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    long countByUserId(String userId);

    List<AccountEntity> findByUserId(String userId);

    Optional<AccountEntity> findByAccountNumber(String accountNumber);
}