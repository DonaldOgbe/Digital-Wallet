package com.deodev.walletService.repository;

import com.deodev.walletService.model.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class WalletRepositoryTest {

    @Autowired
    private WalletRepository testWalletRepository;

    @Test
    public void testItWorks() {
        // given
        Wallet wallet = new Wallet();
        UUID userId = UUID.randomUUID();
        wallet.setUserId(userId);

        // when
        Wallet savedWallet = testWalletRepository.save(wallet);

        // then
        assertTrue(testWalletRepository.existsByUserId(userId));
    }
}