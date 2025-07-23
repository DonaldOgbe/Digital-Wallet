package com.deodev.userService.dto.request;

import lombok.*;

import java.util.UUID;

@Builder
public record CreateWalletRequest(
        UUID userId
) {
}
