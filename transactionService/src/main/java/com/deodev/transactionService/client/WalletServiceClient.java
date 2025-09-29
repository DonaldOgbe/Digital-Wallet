package com.deodev.transactionService.client;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.request.ClientP2PTransferRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "wallet-service",
        url = "${wallet.service.url}")
public interface WalletServiceClient {
    @PostMapping("/api/v1/wallets/accounts/funds/p2p/transfer")
    ResponseEntity<ApiResponse<?>> p2pTransfer(@RequestBody ClientP2PTransferRequest request,
                                @RequestHeader("X-User-Id") String userId);

}
