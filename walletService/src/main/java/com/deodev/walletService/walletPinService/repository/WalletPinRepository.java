package com.deodev.walletService.walletPinService.repository;

import com.deodev.walletService.walletPinService.model.WalletPin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletPinRepository extends JpaRepository<WalletPin, UUID> {
    boolean existsByWalletId(UUID walletId);

    Optional<WalletPin> findByWalletId(UUID walletId);
}
