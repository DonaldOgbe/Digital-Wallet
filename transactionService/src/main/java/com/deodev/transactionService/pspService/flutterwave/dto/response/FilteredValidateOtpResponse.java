package com.deodev.transactionService.pspService.flutterwave.dto.response;

import lombok.Builder;

@Builder
public record FilteredValidateOtpResponse(
        String status,
        String message,
        int id
) {
}
