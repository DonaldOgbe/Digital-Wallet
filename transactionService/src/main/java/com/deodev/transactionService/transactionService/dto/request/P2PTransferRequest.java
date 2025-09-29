package com.deodev.transactionService.transactionService.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record P2PTransferRequest(
        @Size(min = 10, max = 10, message = "Account number must be exactly 10 digits")
        @Pattern(regexp = "\\d+", message = "Account number must contain only digits")
        String senderAccountNumber,

        @Size(min = 10, max = 10, message = "Account number must be exactly 10 digits")
        @Pattern(regexp = "\\d+", message = "Account number must contain only digits")
        String receiverAccountNumber,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        Long amount,

        @Size(min = 4, max = 4, message = "Account number must be exactly 4 digits")
        @Pattern(regexp = "\\d+", message = "Account number must contain only digits")
        String pin
) {
}
