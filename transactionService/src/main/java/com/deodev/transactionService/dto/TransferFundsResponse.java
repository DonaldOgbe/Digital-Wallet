package com.deodev.transactionService.dto;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TransferFundsResponse(
        boolean isSuccess,
        HttpStatus statusCode,
        LocalDateTime timestamp,
        UUID transactionId,
        UUID fundReservationId
) {
}
