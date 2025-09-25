package com.deodev.walletService.walletPinService.controller;

import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.walletPinService.dto.request.SetPinRequest;
import com.deodev.walletService.walletPinService.dto.request.UpdatePinRequest;
import com.deodev.walletService.walletPinService.dto.response.CreateWalletPinResponse;
import com.deodev.walletService.walletPinService.service.WalletPinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets/pin")
public class WalletPinServiceController {

    private final WalletPinService walletPinService;

    @PostMapping
    public ResponseEntity<?> setNewPin(@Valid
                                       @RequestHeader("X-User-Id") String userId,
                                       @RequestBody SetPinRequest requestBody) {
        CreateWalletPinResponse response = walletPinService.createPin(requestBody, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(HttpStatus.CREATED.value(), response)
        );
    }

    @PatchMapping
    public ResponseEntity<?> updatePin(@Valid
                                       @RequestHeader("X-User-Id") String userId,
                                       @RequestBody UpdatePinRequest requestBody) {
        CreateWalletPinResponse response = walletPinService.updatePin(requestBody, userId);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(HttpStatus.OK.value(), response));
    }

}
