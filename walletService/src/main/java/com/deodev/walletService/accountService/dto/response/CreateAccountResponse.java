package com.deodev.walletService.accountService.dto.response;

import com.deodev.walletService.enums.Currency;
import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CreateAccountResponse(
        boolean isSuccess,
        HttpStatus statusCode,
        LocalDateTime timestamp,
        UUID userId,
        UUID walletId,
        UUID accountId,
        String accountNumber,
        Currency currency
) {
}
