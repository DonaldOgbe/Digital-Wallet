package com.deodev.walletService.accountService.dto.request;

import com.deodev.walletService.enums.Currency;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CreateAccountRequest(
        UUID walletId,
        Currency currency
) {
}
