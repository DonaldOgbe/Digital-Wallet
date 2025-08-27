package com.deodev.walletService.walletService.service;

import com.deodev.walletService.walletService.dto.response.CreateWalletResponse;
import com.deodev.walletService.walletService.model.Wallet;
import com.deodev.walletService.walletService.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    @Test
    void testThatWalletIsCreatedAndResponseIsReturned() {
        // given
        UUID userId = UUID.randomUUID();

        Wallet savedWallet = Wallet.builder()
                .userId(userId)
                .build();

        when(walletRepository.save(any())).thenReturn(savedWallet);

        // when
        CreateWalletResponse response = walletService.createWallet(String.valueOf(userId));

        // then
        assertThat(response.success()).isTrue();
        assertThat(response.note()).isEqualTo("Wallet created successfully");
        assertThat(response.userId()).isEqualTo(userId);
    }

}