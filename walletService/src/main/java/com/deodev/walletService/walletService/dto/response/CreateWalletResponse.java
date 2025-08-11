package com.deodev.walletService.walletService.dto.response;

import lombok.*;
import java.util.UUID;


@Builder
public record CreateWalletResponse(
        boolean success,
        String note,
        UUID userId,
        UUID walletId
) {
}
