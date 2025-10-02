package com.deodev.transactionService.transactionService.controller;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.enums.Currency;
import com.deodev.transactionService.redis.RedisCacheService;
import com.deodev.transactionService.transactionService.dto.request.P2PTransferRequest;
import com.deodev.transactionService.transactionService.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final RedisCacheService redisCacheService;
    private final ObjectMapper mapper;

    @PostMapping("/transfer/p2p")
    public ResponseEntity<ApiResponse<?>> p2pTransfer(
            @Valid @RequestBody P2PTransferRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestParam("currency") Currency currency
    ) throws IOException {
        if (idempotencyKey != null) {
            String cached = redisCacheService.getCacheResponse("p2pTransfer:"+idempotencyKey);
            if (cached != null) {
                ApiResponse<?> cachedResponse = mapper.readValue(cached, ApiResponse.class);
                return ResponseEntity.status(cachedResponse.getStatusCode()).body(cachedResponse);
            }
        }

        ApiResponse<?> response = transactionService.processP2PTransfer(request, userId, currency);

        if (idempotencyKey != null) {
            String json = mapper.writeValueAsString(response);
            redisCacheService.cacheResponse("p2pTransfer:"+idempotencyKey, json);
        }

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

}
