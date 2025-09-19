package com.deodev.transactionService.transactionService.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record P2PTransferResponse(
        UUID transactionId,
        String senderAccountNumber,
        String receiverAccountNumber,
        Long amount
) {
}
