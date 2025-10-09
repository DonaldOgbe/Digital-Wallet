package com.deodev.transactionService.transactionService.dto.request;

public record InitiateCardChargeRequest(
        String card_number,
        String expiry_month,
        String expiry_year,
        String cvv,
        String currency,
        Long amount
) {
}
