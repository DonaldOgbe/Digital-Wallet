package com.deodev.transactionService.pspService.flutterwave.controller;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.pspService.flutterwave.dto.request.CompleteChargeCardRequest;
import com.deodev.transactionService.pspService.flutterwave.dto.request.InitiateChargeCardRequest;
import com.deodev.transactionService.pspService.flutterwave.dto.request.ValidateChargeCardRequest;
import com.deodev.transactionService.pspService.flutterwave.dto.request.VerifyChargeCardRequest;
import com.deodev.transactionService.pspService.flutterwave.service.FlutterwaveCardService;
import com.deodev.transactionService.redis.RedisCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/psp/flutterwave")
public class FlutterwaveController {

    private final FlutterwaveCardService flutterwaveCardService;
    private final RedisCacheService redisCacheService;
    private final ObjectMapper mapper;

    @GetMapping("/card/card-type/{bin}")
    public ResponseEntity<ApiResponse<?>> getCardType(@PathVariable String bin) {
        ApiResponse<?> response = flutterwaveCardService.getCardType(bin);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/card/initiate-card-funding")
    public ResponseEntity<ApiResponse<?>> initiateCardFunding(@Valid @RequestBody InitiateChargeCardRequest request,
                                                              @RequestHeader("X-User-Id") String userId,
                                                              @RequestHeader("Idempotency-Key") String idempotencyKey) throws Exception {
        if (idempotencyKey != null) {
            String cached = redisCacheService.getCacheResponse("flw_initiate_card_funding:"+idempotencyKey);
            if (cached != null) {
                ApiResponse<?> cachedResponse = mapper.readValue(cached, ApiResponse.class);
                return ResponseEntity.status(cachedResponse.getStatusCode()).body(cachedResponse);
            }
        }

        ApiResponse<?> response = flutterwaveCardService.initiateChargeCard(request, userId, idempotencyKey);

        if (idempotencyKey != null) {
            String json = mapper.writeValueAsString(response);
            redisCacheService.cacheResponse("flw_initiate_card_funding:"+idempotencyKey, json);
        }
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/card/complete-card-funding")
    public ResponseEntity<ApiResponse<?>> completeCardFunding(@Valid @RequestBody CompleteChargeCardRequest request,
                                                              @RequestHeader("Idempotency-Key") String idempotencyKey) throws Exception {
        if (idempotencyKey != null) {
            String cached = redisCacheService.getCacheResponse("flw_complete_card_funding:"+idempotencyKey);
            if (cached != null) {
                ApiResponse<?> cachedResponse = mapper.readValue(cached, ApiResponse.class);
                return ResponseEntity.status(cachedResponse.getStatusCode()).body(cachedResponse);
            }
        }

        ApiResponse<?> response = flutterwaveCardService.completeChargeCard(request);

        if (idempotencyKey != null) {
            String json = mapper.writeValueAsString(response);
            redisCacheService.cacheResponse("flw_complete_card_funding:"+idempotencyKey, json);
        }
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/card/validate-card-funding")
    public ResponseEntity<ApiResponse<?>> validateCardFunding(@Valid @RequestBody ValidateChargeCardRequest request,
                                                              @RequestHeader("Idempotency-Key") String idempotencyKey) throws Exception {
        if (idempotencyKey != null) {
            String cached = redisCacheService.getCacheResponse("flw_validate_card_funding:"+idempotencyKey);
            if (cached != null) {
                ApiResponse<?> cachedResponse = mapper.readValue(cached, ApiResponse.class);
                return ResponseEntity.status(cachedResponse.getStatusCode()).body(cachedResponse);
            }
        }

        ApiResponse<?> response = flutterwaveCardService.validateChargeCard(request);

        if (idempotencyKey != null) {
            String json = mapper.writeValueAsString(response);
            redisCacheService.cacheResponse("flw_validate_card_funding:"+idempotencyKey, json);
        }
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/card/verify")
    public ResponseEntity<ApiResponse<?>> verifyCardFunding(@Valid @RequestBody VerifyChargeCardRequest request,
                                                            @RequestHeader("Idempotency-Key") String idempotencyKey) throws Exception {
        if (idempotencyKey != null) {
            String cached = redisCacheService.getCacheResponse("flw_verify_card_funding:"+idempotencyKey);
            if (cached != null) {
                ApiResponse<?> cachedResponse = mapper.readValue(cached, ApiResponse.class);
                return ResponseEntity.status(cachedResponse.getStatusCode()).body(cachedResponse);
            }
        }

        ApiResponse<?> response = flutterwaveCardService.verifyCardTransaction(request);

        if (idempotencyKey != null) {
            String json = mapper.writeValueAsString(response);
            redisCacheService.cacheResponse("flw_verify_card_funding:"+idempotencyKey, json);
        }
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }





}
