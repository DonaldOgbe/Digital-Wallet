package com.deodev.walletService.accountService.repository;

import com.deodev.walletService.accountService.model.FundReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FundReservationRepository extends JpaRepository<FundReservation, UUID> {
    boolean existsByAccountNumber(String accountNumber);
    Optional<FundReservation> findByTransactionId(UUID transactionId);
}
