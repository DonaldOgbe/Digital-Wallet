package com.deodev.walletService.accountService.dto.response.request;

import lombok.Builder;


import java.util.UUID;

@Builder
public record ReserveFundsRequest(
        String accountNumber,
        Long amount
) {
}
