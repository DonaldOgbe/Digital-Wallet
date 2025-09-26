package com.deodev.walletService.rabbitmq.events;

import com.deodev.walletService.enums.ErrorCode;

import java.util.UUID;

public record TransferFailedEvent(
        UUID transactionId,
        ErrorCode errorCode,
        String message
) {
}
