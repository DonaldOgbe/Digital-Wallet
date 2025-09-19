package com.deodev.transactionService.dto;


import lombok.Builder;

@Builder
public record ErrorResponse(
        String message,
        String path
) {
}
