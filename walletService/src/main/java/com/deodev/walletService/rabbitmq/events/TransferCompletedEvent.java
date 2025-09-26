package com.deodev.walletService.rabbitmq.events;

import java.util.UUID;

public record TransferCompletedEvent(
        UUID transactionId,
        UUID fundReservationId
) {
}
