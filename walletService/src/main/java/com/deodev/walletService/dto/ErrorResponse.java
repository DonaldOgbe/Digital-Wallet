package com.deodev.walletService.dto;


import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
        LocalDateTime timestamp,
        HttpStatus status,
        String error,
        String message,
        String path
) {
}
