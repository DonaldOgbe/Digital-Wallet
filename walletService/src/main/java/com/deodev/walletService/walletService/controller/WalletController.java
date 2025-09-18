package com.deodev.walletService.walletService.controller;

import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.walletService.dto.response.CreateWalletResponse;
import com.deodev.walletService.walletService.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> createWallet(@RequestAttribute("userId") String userId) {

        CreateWalletResponse response = walletService.createWallet(userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), response));
    }
}
