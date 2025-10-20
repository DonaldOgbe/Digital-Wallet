package com.deodev.transactionService.pspService.walletService.controller;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.enums.Currency;
import com.deodev.transactionService.pspService.walletService.service.WalletService;
import com.deodev.transactionService.redis.RedisCacheService;
import com.deodev.transactionService.transactionService.dto.request.P2PTransferRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/psp/wallet-service")
public class WalletServiceController {

    private final WalletService walletService;
    private final RedisCacheService redisCacheService;
    private final ObjectMapper mapper;

    @PostMapping("p2p/transfer")
    public ResponseEntity<ApiResponse<?>> p2pTransfer(
            @Valid @RequestBody P2PTransferRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) throws IOException {
        if (idempotencyKey != null) {
            String cached = redisCacheService.getCacheResponse("p2pTransfer:"+idempotencyKey);
            if (cached != null) {
                ApiResponse<?> cachedResponse = mapper.readValue(cached, ApiResponse.class);
                return ResponseEntity.status(cachedResponse.getStatusCode()).body(cachedResponse);
            }
        }

        ApiResponse<?> response = walletService.processP2PTransfer(request, userId, idempotencyKey);

        if (idempotencyKey != null) {
            String json = mapper.writeValueAsString(response);
            redisCacheService.cacheResponse("p2pTransfer:"+idempotencyKey, json);
        }

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
