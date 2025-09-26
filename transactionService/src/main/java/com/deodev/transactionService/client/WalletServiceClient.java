package com.deodev.transactionService.client;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.response.ReleaseFundsResponse;
import com.deodev.transactionService.dto.response.ReserveFundsResponse;
import com.deodev.transactionService.dto.response.TransferFundsResponse;
import com.deodev.transactionService.dto.request.ReserveFundsRequest;
import com.deodev.transactionService.dto.request.TransferFundsRequest;
import com.deodev.transactionService.dto.response.ValidateWalletPinResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "wallet-service",
        url = "${wallet.service.url}")
public interface WalletServiceClient {
    @PostMapping("/api/v1/wallets/accounts/funds/reserve")
    ApiResponse<ReserveFundsResponse> reserveFunds(@RequestBody ReserveFundsRequest request,
                                                   @RequestHeader("X-User-Id") String userId);


    @PostMapping("/api/v1/wallets/accounts/funds/{transactionId}/release")
    ApiResponse<ReleaseFundsResponse> releaseFunds(@RequestHeader("Authorization") String AuthToken,
                                                   @PathVariable String transactionId);
}
