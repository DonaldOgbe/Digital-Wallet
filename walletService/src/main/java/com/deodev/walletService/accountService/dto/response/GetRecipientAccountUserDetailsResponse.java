package com.deodev.walletService.accountService.dto.response;

import lombok.Builder;

@Builder
public record GetRecipientAccountUserDetailsResponse(
        String username,
        String firstName,
        String lastName
) {
}
