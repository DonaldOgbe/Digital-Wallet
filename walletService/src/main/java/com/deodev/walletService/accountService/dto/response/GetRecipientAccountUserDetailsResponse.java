package com.deodev.walletService.accountService.dto.response;

import lombok.Builder;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
public record GetRecipientAccountUserDetailsResponse(
        boolean isSuccess,
        HttpStatus statusCode,
        LocalDateTime timestamp,
        String username,
        String firstName,
        String lastName
) {
}
