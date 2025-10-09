package com.deodev.transactionService.pspservice.dto.request;

public record OtpValidateRequest(
        String otp,
        String flw_ref,
        String type
) {
}
