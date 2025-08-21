package com.deodev.walletService.walletPinService.service;

import com.deodev.walletService.exception.PinMismatchException;
import com.deodev.walletService.walletPinService.dto.request.SetPinRequest;
import com.deodev.walletService.walletPinService.dto.request.UpdatePinRequest;
import com.deodev.walletService.walletPinService.dto.response.CreateWalletPinResponse;
import com.deodev.walletService.walletPinService.model.WalletPin;
import com.deodev.walletService.walletPinService.repository.WalletPinRepository;
import lombok.RequiredArgsConstructor;
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
        pinsMatch(request.newPin(), request.confirmNewPin());

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

    public CreateWalletPinResponse updatePin(UpdatePinRequest request, String walletId) {

        WalletPin walletPin = walletPinRepository.findByWalletId(UUID.fromString(walletId)).orElseThrow(
                () -> new IllegalArgumentException("WalletPin not found for walletId: %s".formatted(walletId))
        );

        if (!passwordEncoder.matches(request.oldPin(), walletPin.getPin())) {
            throw new IllegalArgumentException("Incorrect Old Pin");
        }

        pinsMatch(request.newPin(), request.confirmNewPin());

        walletPin.setPin(passwordEncoder.encode(request.newPin()));
        walletPin.setPinUpdatedAt(LocalDateTime.now());

        walletPinRepository.save(walletPin);

        return CreateWalletPinResponse.builder()
                .walletId(walletPin.getWalletId())
                .walletPinId(walletPin.getId())
                .timestamp(LocalDateTime.now())
                .build();
    }

    protected void pinsMatch(String newPin, String confirmNewPin) throws PinMismatchException {
        if (!newPin.equals(confirmNewPin)) {
            throw new PinMismatchException("PINs do not match");
        }
    }
}
