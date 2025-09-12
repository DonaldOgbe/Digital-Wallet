package com.deodev.transactionService.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ReserveFundsRequest(
        @Size(min = 10, max = 10, message = "Account number must be exactly 10 digits")
        @Pattern(regexp = "\\d+", message = "Account number must contain only digits")
        String accountNumber,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        Long amount,

        @NotNull(message = "Transaction ID is required")
        UUID transactionId
) {
}
