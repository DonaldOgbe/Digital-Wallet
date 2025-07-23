package com.deodev.walletService.service;

import com.deodev.walletService.dto.request.CreateWalletRequest;
import com.deodev.walletService.dto.response.CreateWalletResponse;
import com.deodev.walletService.model.Wallet;
import com.deodev.walletService.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public CreateWalletResponse createWallet(CreateWalletRequest request) {

        Wallet wallet = new ObjectMapper().convertValue(request, Wallet.class);
        Wallet savedWallet = walletRepository.save(wallet);

        return CreateWalletResponse.builder()
                .success(true)
                .note("Wallet created successfully")
                .userId(savedWallet.getUserId())
                .walletId(savedWallet.getId())
                .build();
    }
}
