package com.deodev.walletService.walletPinService.dto.response;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CreateWalletPinResponse(
        boolean isSuccess,
        HttpStatus statusCode,
        LocalDateTime timestamp,
        UUID walletId,
        UUID userId,
        UUID walletPinId) {
}
