package com.deodev.transactionService.pspService.flutterwave.dto.response;

import lombok.Builder;

@Builder
public record ValidateChargeCardResponse(
        String status,
        String message,
        Long id
) {
}
