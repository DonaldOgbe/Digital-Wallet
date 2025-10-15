package com.deodev.transactionService.pspService.flutterwave.dto.request;

import lombok.Builder;

@Builder
public record OtpValidateRequest(
        String otp,
        String flw_ref,
        String type
) {
}
