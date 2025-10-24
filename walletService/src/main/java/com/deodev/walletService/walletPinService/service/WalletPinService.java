package com.deodev.walletService.walletPinService.service;

import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.ErrorResponse;
import com.deodev.walletService.enums.ErrorCode;
import com.deodev.walletService.exception.InvalidPinException;
import com.deodev.walletService.exception.ResourceNotFoundException;
import com.deodev.walletService.walletPinService.dto.request.SetPinRequest;
import com.deodev.walletService.walletPinService.dto.request.UpdatePinRequest;
import com.deodev.walletService.walletPinService.dto.response.CreateWalletPinResponse;
import com.deodev.walletService.walletPinService.dto.response.ValidateWalletPinResponse;
import com.deodev.walletService.walletPinService.model.WalletPin;
import com.deodev.walletService.walletPinService.repository.WalletPinRepository;
import com.deodev.walletService.walletService.model.Wallet;
import com.deodev.walletService.walletService.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletPinService {

    private final PasswordEncoder passwordEncoder;

    private final WalletPinRepository walletPinRepository;

    private final WalletService walletService;

    public ApiResponse<?> createPin(SetPinRequest request, String userId) {
        try {
            pinsMatch(request.newPin(), request.confirmNewPin());

            Wallet wallet = walletService.findByUserId(UUID.fromString(userId));

            String hashedPin = passwordEncoder.encode(request.newPin());

            WalletPin walletPin = WalletPin.builder()
                    .walletId(wallet.getId())
                    .userId(UUID.fromString(userId))
                    .pin(hashedPin)
                    .build();

            WalletPin savedPin = walletPinRepository.save(walletPin);

            return ApiResponse.success(HttpStatus.CREATED.value(), CreateWalletPinResponse.builder()
                    .walletId(savedPin.getWalletId())
                    .userId(savedPin.getUserId())
                    .walletPinId(savedPin.getId())
                    .build());
        } catch (Exception ex) {
            log.error("Error while creating wallet pin for user: {}", userId, ex);
            return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ErrorCode.SYSTEM_ERROR,
                    ErrorResponse.builder().message("Error while creating wallet pin").build());
        }

    }

    public CreateWalletPinResponse updatePin(UpdatePinRequest request, String userId) {

        WalletPin walletPin = verifyPin(UUID.fromString(userId), request.oldPin(), "Incorrect Old Pin");

        pinsMatch(request.newPin(), request.confirmNewPin());

        walletPin.setPin(passwordEncoder.encode(request.newPin()));
        walletPin.setPinUpdatedAt(LocalDateTime.now());

        walletPinRepository.save(walletPin);

        return CreateWalletPinResponse.builder()
                .walletId(walletPin.getWalletId())
                .userId(walletPin.getUserId())
                .walletPinId(walletPin.getId())
                .build();
    }

    public void validatePin(UUID userId, String pin) {
        verifyPin(userId, pin, null);

        ValidateWalletPinResponse.builder()
                .isValid(true)
                .build();
    }

    WalletPin verifyPin(UUID userId, String pin, String errorMessage) {
        WalletPin walletPin = walletPinRepository.findByUserId(userId).orElseThrow(
                () -> new ResourceNotFoundException("WalletPin not found for userId: %s".formatted(userId))
        );

        if (!passwordEncoder.matches(pin, walletPin.getPin())) {
            throw new InvalidPinException(errorMessage.isEmpty() ? "Incorrect Pin: %s".formatted(pin) : errorMessage);
        }

        return walletPin;
    }

    void pinsMatch(String pin1, String pin2) throws InvalidPinException {
        if (!pin1.equals(pin2)) {
            throw new InvalidPinException("PINs do not match");
        }
    }
}
