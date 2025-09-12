package com.deodev.transactionService.dto.request;

import lombok.Builder;

import java.util.UUID;

@Builder
public record TransferFundsRequest(
        String accountNumber,
        UUID transactionId
) {
}
