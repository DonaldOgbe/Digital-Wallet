package com.deodev.userService.dto.response;

import lombok.Builder;

import java.util.UUID;


@Builder
public record CreateWalletResponse(
        boolean success,
        String note,
        UUID userId,
        UUID walletId
) {
}
