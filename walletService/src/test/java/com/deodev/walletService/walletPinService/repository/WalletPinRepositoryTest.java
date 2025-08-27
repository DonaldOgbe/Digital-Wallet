package com.deodev.walletService.walletPinService.repository;

import com.deodev.walletService.walletPinService.model.WalletPin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class WalletPinRepositoryTest {

    @Autowired
    private WalletPinRepository testWalletPinRepository;

    @Test
    public void testItWorks() {
        // given
        UUID walletId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        WalletPin walletPin = WalletPin.builder()
                .walletId(walletId)
                .userId(userId)
                .pin("1234")
                .pinUpdatedAt(LocalDateTime.now())
                .build();

        // when
        testWalletPinRepository.save(walletPin);

        // then
        assertThat(testWalletPinRepository.existsByWalletId(walletId)).isTrue();
    }
}