package com.deodev.transactionService.rabbitmq.events;

import lombok.Builder;

@Builder
public record AccountFundedEvent(
        String accountNumber,
        Long amount
) {
}
