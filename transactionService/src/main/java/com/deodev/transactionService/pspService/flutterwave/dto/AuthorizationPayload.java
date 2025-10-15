package com.deodev.transactionService.pspService.flutterwave.dto;

public record AuthorizationPayload(
        String mode,
        String pin
) {
}
