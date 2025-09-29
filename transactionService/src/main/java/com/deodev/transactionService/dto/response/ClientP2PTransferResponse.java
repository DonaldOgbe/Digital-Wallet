package com.deodev.transactionService.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ClientP2PTransferResponse(
        UUID transactionId,
        UUID fundReservationId,
        Long amount,
        String senderAccountNumber,
        String receiverAccountNumber
) {
}
