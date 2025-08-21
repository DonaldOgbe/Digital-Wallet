package com.deodev.walletService.walletPinService.controller;

import com.deodev.walletService.walletPinService.dto.request.SetPinRequest;
import com.deodev.walletService.walletPinService.dto.request.UpdatePinRequest;
import com.deodev.walletService.walletPinService.dto.response.CreateWalletPinResponse;
import com.deodev.walletService.walletPinService.service.WalletPinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets")
@Validated
public class WalletPinServiceController {

    private final WalletPinService walletPinService;

    @PostMapping("/{walletId}/pin")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> setNewPin(@Valid
                                       @PathVariable String walletId,
                                       @RequestBody SetPinRequest requestBody) {
        CreateWalletPinResponse response = walletPinService.createPin(requestBody, walletId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PatchMapping("/{walletId}/pin")
    public ResponseEntity<?> updatePin(@Valid
                                       @PathVariable String walletId,
                                       @RequestBody UpdatePinRequest requestBody) {
        CreateWalletPinResponse response = walletPinService.updatePin(requestBody, walletId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
