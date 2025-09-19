package com.deodev.walletService.accountService.dto.response;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReserveFundsResponse(
        UUID fundReservationId
) {
}
