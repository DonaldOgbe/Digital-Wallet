package com.deodev.walletService.accountService.dto.response;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
public record ReserveFundsResponse(
        boolean isSuccess,
        HttpStatus status,
        LocalDateTime timestamp
) {
}
