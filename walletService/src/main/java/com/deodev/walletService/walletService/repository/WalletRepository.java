package com.deodev.walletService.walletService.repository;

import com.deodev.walletService.walletService.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    boolean existsByUserId(UUID userId);
    Optional<Wallet> findByUserId(UUID userId);
}
