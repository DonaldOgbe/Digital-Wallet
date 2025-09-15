package com.deodev.userService.rabbitmq.event;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserRegisteredEvent(
        UUID userid
) {
}
