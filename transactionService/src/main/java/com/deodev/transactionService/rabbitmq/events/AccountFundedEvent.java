package com.deodev.transactionService.rabbitmq.events;

import lombok.Builder;

@Builder
public record AccountFundedEvent(
        String eventId,
        String accountNumber,
        Long amount
) {
}
