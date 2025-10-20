package com.deodev.transactionService.pspService.flutterwave.dto.request;

import lombok.Builder;

@Builder
public record ValidateChargeCardRequest(
        String txn_ref,
        String otp,
        String flw_ref
) {
}
