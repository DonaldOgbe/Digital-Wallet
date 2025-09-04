package com.deodev.walletService.accountService.dto.response;

import lombok.Builder;

@Builder
public record ReserveFundsResponse(
        boolean isSuccess,
        String errorCode
) {
}
