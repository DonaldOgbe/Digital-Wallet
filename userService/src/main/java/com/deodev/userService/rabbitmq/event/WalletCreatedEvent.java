package com.deodev.userService.rabbitmq.event;

import java.util.UUID;

public record WalletCreatedEvent(
        UUID userId
) {
}
