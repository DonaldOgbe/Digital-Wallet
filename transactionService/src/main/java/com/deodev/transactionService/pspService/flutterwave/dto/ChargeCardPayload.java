package com.deodev.transactionService.pspService.flutterwave.dto;

import lombok.Builder;

@Builder
public record ChargeCardPayload(
        String card_number,
        String expiry_month,
        String expiry_year,
        String cvv,
        String currency,
        Long amount,
        String email,
        String tx_ref
) {
}
