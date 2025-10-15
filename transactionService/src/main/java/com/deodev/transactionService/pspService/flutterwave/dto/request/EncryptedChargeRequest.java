package com.deodev.transactionService.pspService.flutterwave.dto.request;

import lombok.Builder;

@Builder
public record EncryptedChargeRequest(
        String client
) {
}
