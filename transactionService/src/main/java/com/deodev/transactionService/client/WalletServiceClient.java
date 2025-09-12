package com.deodev.transactionService.client;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.ReleaseFundsResponse;
import com.deodev.transactionService.dto.ReserveFundsResponse;
import com.deodev.transactionService.dto.TransferFundsResponse;
import com.deodev.transactionService.dto.request.ReserveFundsRequest;
import com.deodev.transactionService.dto.request.TransferFundsRequest;
import com.deodev.transactionService.dto.response.ValidateWalletPinResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "wallet-service",
        url = "${wallet.service.url}")
public interface WalletServiceClient {
    @PostMapping("/api/v1/wallets/pin/validate")
    ApiResponse<ValidateWalletPinResponse> validatePin(@RequestHeader("Authorization") String AuthToken,
                                                              @RequestHeader("Wallet-Pin") String pin);

    @PostMapping("/api/v1/wallets/accounts/funds/reserve")
    ApiResponse<ReserveFundsResponse> reserveFunds(@RequestHeader("Authorization") String AuthToken,
                                                          @RequestBody ReserveFundsRequest request);

    @PostMapping("/api/v1/wallets/accounts/funds/transfer")
    ApiResponse<TransferFundsResponse> transferFunds(@RequestHeader("Authorization") String AuthToken,
                                                            @RequestBody TransferFundsRequest request);

    @PostMapping("/api/v1/wallets/accounts/funds/release/{transactionId}")
    ApiResponse<ReleaseFundsResponse> releaseFunds(@RequestHeader("Authorization") String AuthToken,
                                                   @PathVariable String transactionId);
}
