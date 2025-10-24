package com.deodev.transactionService.pspService.flutterwave.client;

import com.deodev.transactionService.exception.ExternalServiceException;
import com.deodev.transactionService.pspService.flutterwave.dto.FlutterwaveResponse;
import com.deodev.transactionService.pspService.flutterwave.dto.request.EncryptedChargeRequest;
import com.deodev.transactionService.pspService.flutterwave.dto.request.OtpValidateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;


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

    public FlutterwaveResponse resolveCard(String bin) {
        try {
            return webClient.get()
                    .uri("/v3/card-bins/{bin}", bin)
                    .header("Authorization", "Bearer " + secretKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .exchangeToMono(clientResponse -> {
                        if (clientResponse.statusCode().is2xxSuccessful()) {
                            log.info("Successfully resolved card BIN [{}]", bin);
                            return clientResponse.bodyToMono(FlutterwaveResponse.class);
                        } else if (clientResponse.statusCode().is4xxClientError()) {
                            log.warn("Flutterwave card bin resolution failed for BIN [{}]: Client error {}", bin, clientResponse.statusCode());
                            return clientResponse.bodyToMono(FlutterwaveResponse.class);
                        } else {
                            log.error("Flutterwave card bin resolution failed for BIN [{}]: Server error {}", bin, clientResponse.statusCode());
                            return clientResponse.createException().flatMap(Mono::error);
                        }
                    })
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException for request [{}]: response {}", ex.getRequest(), ex.getResponseBodyAsString(), ex);
            throw new ExternalServiceException("Flutterwave card bin resolution failed", ex);
        } catch (Exception ex) {
            log.error("Unexpected error resolving card BIN  [{}]: {}", bin, ex.getMessage(), ex);
            throw ex;
        }
    }

    public FlutterwaveResponse chargeCard(EncryptedChargeRequest request) {
        try {
            return webClient.post()
                    .uri("/v3/charges?type=card")
                    .header("Authorization", "Bearer " + secretKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .bodyValue(request)
                    .exchangeToMono(clientResponse -> {
                        if (clientResponse.statusCode().is2xxSuccessful()) {
                            log.info("Successfully charged card");
                            return clientResponse.bodyToMono(FlutterwaveResponse.class);
                        } else if (clientResponse.statusCode().is4xxClientError()) {
                            log.warn("Flutterwave charge card failed: Client error {}", clientResponse.statusCode());
                            return clientResponse.bodyToMono(FlutterwaveResponse.class);
                        } else {
                            log.error("Flutterwave charge card failed: Server error {}", clientResponse.statusCode());
                            return clientResponse.createException().flatMap(Mono::error);
                        }
                    })
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException for request [{}]: response {}", ex.getRequest(), ex.getResponseBodyAsString(), ex);
            throw new ExternalServiceException("Flutterwave charge card failed", ex);
        } catch (Exception ex) {
            log.error("Unexpected error charging card: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    public FlutterwaveResponse validateCharge(OtpValidateRequest request) {
        try {
            return webClient.post()
                    .uri("/v3/validate-charge")
                    .header("Authorization", "Bearer " + secretKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .exchangeToMono(clientResponse -> {
                        if (clientResponse.statusCode().is2xxSuccessful()) {
                            log.info("Successfully validated card charge");
                            return clientResponse.bodyToMono(FlutterwaveResponse.class);
                        } else if (clientResponse.statusCode().is4xxClientError()) {
                            log.warn("Flutterwave validate card charge failed: Client error {}", clientResponse.statusCode());
                            return clientResponse.bodyToMono(FlutterwaveResponse.class);
                        } else {
                            log.error("Flutterwave validate card charge failed: Server error {}", clientResponse.statusCode());
                            return clientResponse.createException().flatMap(Mono::error);
                        }
                    })
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException for request [{}]: response {}", ex.getRequest(), ex.getResponseBodyAsString(), ex);
            throw new ExternalServiceException("Flutterwave validate card charge failed", ex);
        } catch (Exception ex) {
            log.error("Unexpected error validating card charge for flw_ref: [{}], {}", request.flw_ref(), ex.getMessage(), ex);
            throw ex;
        }
    }

    public FlutterwaveResponse verifyCharge(Long transactionId) {
        try {
            return webClient.get()
                    .uri("/v3/transactions/{transactionId}/verify", transactionId)
                    .header("Authorization", "Bearer " + secretKey)
                    .header("Content-Type", "application/json")
                    .exchangeToMono(clientResponse -> {
                        if (clientResponse.statusCode().is2xxSuccessful()) {
                            log.info("Successfully verified card charge");
                            return clientResponse.bodyToMono(FlutterwaveResponse.class);
                        } else if (clientResponse.statusCode().is4xxClientError()) {
                            log.warn("Flutterwave verify card charge failed: Client error {}", clientResponse.statusCode());
                            return clientResponse.bodyToMono(FlutterwaveResponse.class);
                        } else {
                            log.error("Flutterwave verify card charge failed: Server error {}", clientResponse.statusCode());
                            return clientResponse.createException().flatMap(Mono::error);
                        }
                    })
                    .block();
        } catch (WebClientResponseException ex) {
            log.error("WebClientResponseException for request [{}]: response {}", ex.getRequest(), ex.getResponseBodyAsString(), ex);
            throw new ExternalServiceException("Flutterwave verify card charge failed", ex);
        } catch (Exception ex) {
            log.error("Unexpected error while verifying card charge for id: [{}], {}", transactionId, ex.getMessage(), ex);
            throw ex;
        }

    }

}
