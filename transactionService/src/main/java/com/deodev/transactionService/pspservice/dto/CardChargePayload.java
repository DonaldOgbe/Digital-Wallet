package com.deodev.transactionService.pspservice.dto;

public record CardChargePayload(
        String card_number,
        String expiry_month,
        String expiry_year,
        String cvv,
        String currency,
        Long amount,
        String email,
        String fullname,
        String tx_ref
) {
}
