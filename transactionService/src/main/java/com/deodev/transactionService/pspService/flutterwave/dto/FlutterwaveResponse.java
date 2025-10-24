package com.deodev.transactionService.pspService.flutterwave.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record FlutterwaveResponse(
        String status,
        String message,
        Map<String, Object> data,
        Map<String, Object> meta
) {
}
