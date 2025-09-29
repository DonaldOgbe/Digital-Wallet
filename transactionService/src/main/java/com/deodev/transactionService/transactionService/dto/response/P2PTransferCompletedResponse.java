package com.deodev.transactionService.transactionService.dto.response;

import com.deodev.transactionService.enums.Currency;
import com.deodev.transactionService.enums.TransactionStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record P2PTransferCompletedResponse(
        UUID transactionId,
        String senderAccountNumber,
        String receiverAccountNumber,
        Long amount,
        Currency currency,
        TransactionStatus status,
        LocalDateTime timestamp
) {
}
