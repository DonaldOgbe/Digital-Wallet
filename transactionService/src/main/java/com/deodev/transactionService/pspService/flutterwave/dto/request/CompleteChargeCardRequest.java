package com.deodev.transactionService.pspService.flutterwave.dto.request;

import lombok.Builder;

@Builder
public record CompleteChargeCardRequest(
        String txn_ref,
        String client
) {
}
