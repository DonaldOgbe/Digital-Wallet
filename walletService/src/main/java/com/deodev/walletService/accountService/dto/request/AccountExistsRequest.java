package com.deodev.walletService.accountService.dto.request;

import com.deodev.walletService.enums.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AccountExistsRequest(
        @Size(min = 10, max = 10, message = "Account number must be exactly 10 digits")
        @Pattern(regexp = "\\d+", message = "Account number must contain only digits")
        String accountNumber,

        @NotNull(message = "Currency is required")
        Currency currency
) {
}
