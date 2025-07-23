package com.deodev.walletService.controller;

import com.deodev.walletService.dto.request.CreateWalletRequest;
import com.deodev.walletService.dto.response.CreateWalletResponse;
import com.deodev.walletService.service.WalletService;
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
@Validated
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> createWallet(@Valid @RequestBody CreateWalletRequest body) {

        CreateWalletResponse response = walletService.createWallet(body);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
