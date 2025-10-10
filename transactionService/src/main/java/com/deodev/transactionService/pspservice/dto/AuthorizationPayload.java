package com.deodev.transactionService.pspservice.dto;

public record AuthorizationPayload(
        String mode,
        String pin
) {
}
