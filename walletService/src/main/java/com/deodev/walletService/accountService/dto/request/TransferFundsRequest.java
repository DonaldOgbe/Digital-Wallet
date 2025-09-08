package com.deodev.walletService.accountService.dto.request;

import com.deodev.walletService.enums.Currency;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.util.UUID;

@Builder
public record TransferFundsRequest(
        @Size(min = 10, max = 10, message = "Account number must be exactly 10 digits")
        @Pattern(regexp = "\\d+", message = "Account number must contain only digits")
        String accountNumber,

        @NotNull(message = "Currency is required")
        Currency currency,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        Long amount,

        @NotNull(message = "Transaction ID is required")
        UUID transactionId
) {
}
