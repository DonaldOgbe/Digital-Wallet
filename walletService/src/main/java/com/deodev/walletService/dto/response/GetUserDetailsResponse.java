package com.deodev.walletService.dto.response;

import lombok.Builder;

@Builder
public record GetUserDetailsResponse(
        String username,
        String firstName,
        String lastName,
        String email
) {
}
