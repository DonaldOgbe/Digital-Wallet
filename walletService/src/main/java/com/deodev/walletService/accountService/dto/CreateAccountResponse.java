package com.deodev.walletService.accountService.dto;

import com.deodev.walletService.enums.Currency;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record CreateAccountResponse(
        UUID userId,
        UUID walletId,
        UUID accountId,
        String accountNumber,
        Currency currency,
        LocalDateTime timestamp
) {
}
