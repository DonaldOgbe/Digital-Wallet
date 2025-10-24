package com.deodev.walletService.dto.response;

import lombok.Builder;

@Builder
public record GetUserDetailsResponse(
        String firstname,
        String lastname,
        String email
) {
}
