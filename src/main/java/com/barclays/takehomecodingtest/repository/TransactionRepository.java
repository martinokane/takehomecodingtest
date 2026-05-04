package com.barclays.takehomecodingtest.repository;

import com.barclays.takehomecodingtest.model.TransactionEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByAccountAccountNumber(String accountNumber);
}
