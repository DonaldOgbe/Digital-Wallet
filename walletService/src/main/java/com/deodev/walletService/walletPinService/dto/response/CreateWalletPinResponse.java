package com.deodev.walletService.walletPinService.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CreateWalletPinResponse(
        UUID walletId,
        UUID walletPinId,
        LocalDateTime timestamp) {
}
