package com.deodev.walletService.rabbitmq.events;

import java.util.UUID;

public record WalletCreatedEvent(
        UUID userId
) {
}
