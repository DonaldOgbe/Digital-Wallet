package com.deodev.transactionService.pspservice.client;

import com.deodev.transactionService.exception.ExternalServiceException;
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
                    .uri("/card-bins/{bin}", bin)
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

}
