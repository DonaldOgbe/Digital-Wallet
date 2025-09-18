package com.deodev.walletService.dto.response;

import lombok.Builder;

@Builder
public record GetUserDetailsResponse(
        String firstName,
        String lastName,
        String email
) {
}
