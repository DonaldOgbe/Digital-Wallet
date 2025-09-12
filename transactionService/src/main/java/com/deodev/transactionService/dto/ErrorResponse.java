package com.deodev.transactionService.dto;


import com.deodev.transactionService.enums.ErrorCode;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(
        LocalDateTime timestamp,
        HttpStatus statusCode,
        ErrorCode errorCode,
        String message,
        String path
) {
}
