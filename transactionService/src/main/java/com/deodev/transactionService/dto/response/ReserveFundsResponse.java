package com.deodev.transactionService.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ReserveFundsResponse(
        UUID fundReservationId
) {
}
