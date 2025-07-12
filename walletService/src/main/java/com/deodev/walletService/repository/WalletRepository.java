package com.deodev.walletService.repository;

import com.deodev.walletService.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletRepository extends JpaRepository<UUID, Wallet> {
}
