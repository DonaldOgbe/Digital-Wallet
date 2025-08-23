package com.deodev.walletService.walletService.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Builder
public record CreateWalletRequest(
        @NotNull(message = "user ID cannot be null")
        UUID userId
) {
}
