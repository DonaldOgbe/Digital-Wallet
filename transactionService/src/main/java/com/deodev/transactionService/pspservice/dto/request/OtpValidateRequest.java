package com.deodev.transactionService.pspservice.dto.request;

import lombok.Builder;

@Builder
public record OtpValidateRequest(
        String otp,
        String flw_ref,
        String type
) {
}
