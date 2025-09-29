package com.deodev.walletService.dto;

import lombok.Builder;

@Builder
public record ErrorResponse(
        String message,
        String path
) {
}
