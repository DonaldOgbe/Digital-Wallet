package com.deodev.walletService.walletService.controller;

import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.walletService.dto.response.CreateWalletResponse;
import com.deodev.walletService.walletService.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<?> createWallet(@RequestHeader("X-User-Id") String userId) {

        CreateWalletResponse response = walletService.createWallet(userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), response));
    }
}
