package com.deodev.transactionService.transactionService.repository;

import com.deodev.transactionService.transactionService.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    boolean existsById(UUID id);

    Optional<Transaction> findByIdempotencyKey(String key);
}
