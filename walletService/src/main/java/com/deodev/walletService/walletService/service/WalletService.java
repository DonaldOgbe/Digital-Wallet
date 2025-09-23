package com.deodev.walletService.walletService.service;

import com.deodev.walletService.exception.ResourceNotFoundException;
import com.deodev.walletService.rabbitmq.publisher.WalletEventsPublisher;
import com.deodev.walletService.walletService.dto.response.CreateWalletResponse;
import com.deodev.walletService.walletService.model.Wallet;
import com.deodev.walletService.walletService.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletEventsPublisher walletEventsPublisher;

    public CreateWalletResponse createWallet(String userId) {
        Wallet wallet = Wallet.builder()
                .userId(UUID.fromString(userId))
                .build();
        Wallet savedWallet = walletRepository.save(wallet);

        walletEventsPublisher.publishWalletCreated(UUID.fromString(userId));

        return CreateWalletResponse.builder()
                .userId(savedWallet.getUserId())
                .walletId(savedWallet.getId())
                .build();
    }

    public Wallet findByUserId(UUID userId) {
        return walletRepository.findByUserId(userId).orElseThrow(
                () -> new ResourceNotFoundException("Wallet not found for userId: %s".formatted(userId))
        );
    }
}
