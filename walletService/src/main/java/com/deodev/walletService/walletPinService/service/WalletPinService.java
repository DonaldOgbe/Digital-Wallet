package com.deodev.walletService.walletPinService.service;

import com.deodev.walletService.exception.PinMismatchException;
import com.deodev.walletService.walletPinService.dto.SetPinRequest;
import com.deodev.walletService.walletPinService.dto.response.CreateWalletPinResponse;
import com.deodev.walletService.walletPinService.model.WalletPin;
import com.deodev.walletService.walletPinService.repository.WalletPinRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletPinService {

    private final PasswordEncoder passwordEncoder;

    private final WalletPinRepository walletPinRepository;

    public CreateWalletPinResponse createPin(SetPinRequest request, String walletId) {
        if (!pinsMatch(request)) {
            throw new PinMismatchException("PINs do not match");
        }

        String hashedPin = passwordEncoder.encode(request.newPin());

        WalletPin walletPin = WalletPin.builder()
                .walletId(UUID.fromString(walletId))
                .pin(hashedPin)
                .build();

        WalletPin savedPin = walletPinRepository.save(walletPin);

        return CreateWalletPinResponse.builder()
                .walletId(savedPin.getWalletId())
                .walletPinId(savedPin.getId())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private boolean pinsMatch(SetPinRequest request) {
        return request.newPin() != null &&
                request.newPin().equals(request.confirmNewPin());
    }
}
