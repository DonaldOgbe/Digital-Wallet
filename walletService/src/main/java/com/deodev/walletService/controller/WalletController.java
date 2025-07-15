package com.deodev.walletService.controller;

import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.request.CreateWalletRequest;
import com.deodev.walletService.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping("/create")
    private ResponseEntity<?> createWallet(@RequestBody @Valid CreateWalletRequest body) {

        ApiResponse<?> response = walletService.createWallet(body);

        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }
}
