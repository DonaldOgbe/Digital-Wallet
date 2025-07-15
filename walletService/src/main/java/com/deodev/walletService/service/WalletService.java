package com.deodev.walletService.service;

import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.request.CreateWalletRequest;
import com.deodev.walletService.dto.response.CreateWalletResponse;
import com.deodev.walletService.model.Wallet;
import com.deodev.walletService.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public ApiResponse<?> createWallet(CreateWalletRequest request) {

        Wallet wallet = new ObjectMapper().convertValue(request, Wallet.class);
        walletRepository.save(wallet);

        return ApiResponse.<CreateWalletResponse>builder()
                .message("Wallet Created successfully")
                .status(HttpStatus.CREATED)
                .data(CreateWalletResponse.builder()
                        .userId(request.getUserId())
                        .build())
                .build();
    }

}
