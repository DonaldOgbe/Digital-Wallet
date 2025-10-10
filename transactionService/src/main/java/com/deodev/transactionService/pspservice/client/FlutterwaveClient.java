package com.deodev.transactionService.pspservice.client;

import com.deodev.transactionService.exception.ExternalServiceException;
import com.deodev.transactionService.pspservice.dto.request.ClientChargeRequest;
import com.deodev.transactionService.pspservice.dto.request.OtpValidateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;

@Service
@Slf4j
public class FlutterwaveClient {
    private final WebClient webClient;

    @Value("${psp.flutterwave.secret-key}")
    private String secretKey;

    public FlutterwaveClient(@Value("${psp.flutterwave.url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Map<String, Object> resolveCard(String bin) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri("/v3/card-bins/{bin}", bin)
                    .header("Authorization", "Bearer " + secretKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return response;
        } catch (WebClientResponseException e) {
            log.error("Flutterwave API error while resolving card BIN {}: {}", bin, e.getResponseBodyAsString(), e);
            throw new ExternalServiceException("Flutterwave API error while resolving card BIN", e);
        } catch (Exception e) {
            log.error("Unexpected error resolving card BIN {}: {}", bin, e.getMessage(), e);
            throw e;
        }
    }

    public Map<String, Object> chargeCard(ClientChargeRequest request) {
        try {
            return webClient.post()
                    .uri("/v3/charges?type=card")
                    .header("Authorization", "Bearer " + secretKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.error("Error calling Flutterwave chargeCard API: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Map<String, Object> validateCharge(OtpValidateRequest request) {
        try {
            return webClient.post()
                    .uri("/v3/validate-charge")
                    .header("Authorization", "Bearer " + secretKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.error("Error validating charge Flutterwave charge: {}, {}", request, e.getMessage(), e);
            throw e;
        }
    };

    public Map<String, Object> verifyCharge(String transactionId) {
        try {
            return webClient.get()
                    .uri("/v3/transactions/{transactionId}/verify", transactionId)
                    .header("Authorization", "Bearer " + secretKey)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.error("Error verifying Flutterwave charge: {}", e.getMessage(), e);
            throw e;
        }

    }

}
