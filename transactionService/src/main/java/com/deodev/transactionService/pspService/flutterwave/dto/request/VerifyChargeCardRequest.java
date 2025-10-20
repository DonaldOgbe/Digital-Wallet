package com.deodev.transactionService.pspService.flutterwave.dto.request;

import lombok.Builder;

@Builder
public record VerifyChargeCardRequest(
        Long id,
        String txn_ref
) {
}
