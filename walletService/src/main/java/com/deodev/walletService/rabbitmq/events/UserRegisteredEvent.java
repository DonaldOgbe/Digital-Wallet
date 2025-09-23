package com.deodev.walletService.rabbitmq.events;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserRegisteredEvent(
        UUID userid
) {
}
