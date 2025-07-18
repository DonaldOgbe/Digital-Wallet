package com.deodev.userService.client;

import com.deodev.userService.dto.request.CreateWalletRequest;
import com.deodev.userService.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;

public class WalletServiceFallback implements WalletServiceClient{

    @Override
    public ApiResponse<?> createWallet(CreateWalletRequest body, String authHeader) {
        return ApiResponse.error(
                "Wallet Service is currently unavailable",
                "SERVICE UNAVAILABLE",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}
