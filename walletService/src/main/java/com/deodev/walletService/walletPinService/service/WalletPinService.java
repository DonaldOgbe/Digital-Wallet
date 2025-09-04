package com.deodev.walletService.walletPinService.service;

import com.deodev.walletService.exception.PinMismatchException;
import com.deodev.walletService.walletPinService.dto.request.SetPinRequest;
import com.deodev.walletService.walletPinService.dto.request.UpdatePinRequest;
import com.deodev.walletService.walletPinService.dto.response.CreateWalletPinResponse;
import com.deodev.walletService.walletPinService.dto.response.ValidateWalletPinResponse;
import com.deodev.walletService.walletPinService.model.WalletPin;
import com.deodev.walletService.walletPinService.repository.WalletPinRepository;
import com.deodev.walletService.walletService.model.Wallet;
import com.deodev.walletService.walletService.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.deodev.walletService.common.ErrorCodes.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletPinService {

    private final PasswordEncoder passwordEncoder;

    private final WalletPinRepository walletPinRepository;

    private final WalletRepository walletRepository;

    public CreateWalletPinResponse createPin(SetPinRequest request, String userId) {
        pinsMatch(request.newPin(), request.confirmNewPin());

        String hashedPin = passwordEncoder.encode(request.newPin());

        Wallet wallet = walletRepository.findByUserId(UUID.fromString(userId)).orElseThrow(
                () -> new IllegalArgumentException("Wallet not found for userId: %s".formatted(userId))
        );

        WalletPin walletPin = WalletPin.builder()
                .walletId(wallet.getId())
                .userId(UUID.fromString(userId))
                .pin(hashedPin)
                .build();

        WalletPin savedPin = walletPinRepository.save(walletPin);

        return CreateWalletPinResponse.builder()
                .walletId(savedPin.getWalletId())
                .userId(savedPin.getUserId())
                .walletPinId(savedPin.getId())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public CreateWalletPinResponse updatePin(UpdatePinRequest request, String userId) {

        WalletPin walletPin = walletPinRepository.findByUserId(UUID.fromString(userId)).orElseThrow(
                () -> new IllegalArgumentException("WalletPin not found for userId: %s".formatted(userId))
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
                .userId(walletPin.getUserId())
                .walletPinId(walletPin.getId())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public ValidateWalletPinResponse validatePin(String userId, String pin) {
        try {
            WalletPin walletPin = walletPinRepository.findByUserId(UUID.fromString(userId))
                    .orElseThrow(() -> new IllegalArgumentException("WalletPin not found for userId: %s".formatted(userId)));


            if (!passwordEncoder.matches(pin, walletPin.getPin())) {
                throw new PinMismatchException("Incorrect Pin: %s".formatted(pin));
            }

            return ValidateWalletPinResponse.builder()
                    .isValid(true)
                    .build();
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            return ValidateWalletPinResponse.builder()
                    .isValid(false)
                    .errorCode(NOT_FOUND)
                    .build();
        } catch (PinMismatchException e) {
            log.error(e.getMessage(), e);
            return ValidateWalletPinResponse.builder()
                    .isValid(false)
                    .errorCode(INVALID_PIN)
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ValidateWalletPinResponse.builder()
                    .isValid(false)
                    .errorCode(SYSTEM_ERROR)
                    .build();
        }
    }

    protected void pinsMatch(String pin1, String pin2) throws PinMismatchException {
        if (!pin1.equals(pin2)) {
            throw new PinMismatchException("PINs do not match");
        }
    }
}
