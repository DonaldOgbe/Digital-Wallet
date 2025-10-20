package com.deodev.transactionService.pspService.flutterwave.client;

import com.deodev.transactionService.exception.ExternalServiceException;
import com.deodev.transactionService.pspService.flutterwave.dto.request.EncryptedChargeRequest;
import com.deodev.transactionService.pspService.flutterwave.dto.request.OtpValidateRequest;
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
    private final String secretKey;


    public FlutterwaveClient(@Value("${psp.flutterwave.url}") String baseUrl,
                             @Value("${psp.flutterwave.secret-key}") String secretKey) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.secretKey = secretKey;
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

            validateResponse(response, "Empty response body while resolving card BIN "+ bin);
            return response;
        } catch (WebClientResponseException e) {
            log.error("Flutterwave API error while resolving card BIN {}: {}", bin, e.getResponseBodyAsString(), e);
            throw new ExternalServiceException("Flutterwave API error while resolving card BIN", e);
        } catch (Exception e) {
            log.error("Unexpected error resolving card BIN {}: {}", bin, e.getMessage(), e);
            throw e;
        }
    }

    public Map<String, Object> chargeCard(EncryptedChargeRequest request) {
        try {
            Map<String, Object> response =  webClient.post()
                    .uri("/v3/charges?type=card")
                    .header("Authorization", "Bearer " + secretKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            validateResponse(response, "Empty response body from charge card");
            return response;
        } catch (WebClientResponseException e) {
            log.error("Flutterwave API error while calling charge card  {}", e.getResponseBodyAsString(), e);
            throw new ExternalServiceException("Flutterwave API error while calling charge card", e);
        } catch (Exception e) {
            log.error("Unexpected error while charging card: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Map<String, Object> validateCharge(OtpValidateRequest request) {
        try {
            Map<String, Object> response = webClient.post()
                    .uri("/v3/validate-charge")
                    .header("Authorization", "Bearer " + secretKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            validateResponse(response, "Empty response body from validate charge card");
            return response;
        } catch (WebClientResponseException e) {
            log.error("Flutterwave API error while validating card charge for flw_ref: {}, {}", request.flw_ref(), e.getResponseBodyAsString(), e);
            throw new ExternalServiceException("Flutterwave API error validating card charge", e);
        } catch (Exception e) {
            log.error("Error validating card charge Flutterwave charge for flw_ref: {}, {}", request.flw_ref(), e.getMessage(), e);
            throw e;
        }
    };

    public Map<String, Object> verifyCharge(Long transactionId) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri("/v3/transactions/{transactionId}/verify", transactionId)
                    .header("Authorization", "Bearer " + secretKey)
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            validateResponse(response, "Empty response body from verify charge");
            return response;
        } catch (WebClientResponseException e) {
            log.error("Flutterwave API error while verifying card charge for id: {}, {}", transactionId, e.getResponseBodyAsString(), e);
            throw new ExternalServiceException("Flutterwave API error while verifying card charge", e);
        } catch (Exception e) {
            log.error("Unexpected Error while verifying card charge for id: {}, {}", transactionId, e.getMessage(), e);
            throw e;
        }

    }

    void validateResponse(Map<String, Object> response, String message) {
        if (response == null) {
            log.warn(message);
            throw new ExternalServiceException("Empty response from Flutterwave");
        }
    }

}
