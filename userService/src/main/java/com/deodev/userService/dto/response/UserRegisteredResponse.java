package com.deodev.userService.dto.response;

import com.deodev.userService.model.enums.UserStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserRegisteredResponse(
        UUID userId,
        UUID walletId,
        String username,
        String email,
        UserStatus status,
        LocalDateTime registeredAt
) {
}
