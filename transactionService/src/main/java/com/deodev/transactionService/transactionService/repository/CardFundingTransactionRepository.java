package com.deodev.transactionService.transactionService.repository;

import com.deodev.transactionService.transactionService.model.CardFundingTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CardFundingTransactionRepository extends JpaRepository<CardFundingTransaction, UUID> {
    boolean existsByGatewayReference(String gatewayReference);
}
