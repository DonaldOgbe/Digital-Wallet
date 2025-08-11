package com.deodev.walletService.accountService.repository;

import com.deodev.walletService.accountService.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    boolean existsByWalletId(UUID walletId);
}
