package com.deodev.transactionService.pspservice.service;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.pspservice.client.FlutterwaveClient;
import com.deodev.transactionService.pspservice.dto.response.CardTypePayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlutterwaveService {

    private final FlutterwaveClient flutterwaveClient;

    public ApiResponse<?> getCardType(String bin) {
        Map<String, Object> response;

        response = flutterwaveClient.resolveCard(bin);

        if (!"success".equalsIgnoreCase((String) response.get("status"))) {
            log.warn("Flutterwave failed to resolve card type for BIN {}. Response: {}", bin, response);
            return ApiResponse.success(HttpStatus.OK.value(), new CardTypePayload("UNKNOWN"));
        }

        Map<String, Object> data = (Map<String, Object>) response.get("data");
        String cardType = (data != null) ? (String) data.getOrDefault("card_type", "UNKNOWN") : "UNKNOWN";

        return ApiResponse.success(HttpStatus.OK.value(), new CardTypePayload(cardType));
    }
}
