package com.deodev.transactionService.transactionService.repository;

import com.deodev.transactionService.transactionService.model.P2PTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface P2PTransactionRepository extends JpaRepository<P2PTransaction, UUID> {
    boolean existsById(UUID id);
}
