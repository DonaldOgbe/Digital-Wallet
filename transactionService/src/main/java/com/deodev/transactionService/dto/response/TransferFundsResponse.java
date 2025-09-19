package com.deodev.transactionService.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record TransferFundsResponse(
        UUID transactionId,
        UUID fundReservationId
) {
}
