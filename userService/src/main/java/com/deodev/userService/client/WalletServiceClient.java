package com.deodev.userService.client;

import com.deodev.userService.dto.request.CreateWalletRequest;
import com.deodev.userService.dto.response.ApiResponse;
import com.deodev.userService.dto.response.CreateWalletResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "wallet-service",
        url = "${wallet.service.url}")
public interface WalletServiceClient {

    @PostMapping("/api/v1/wallets")
    CreateWalletResponse createWallet(@RequestBody CreateWalletRequest body,
                                      @RequestHeader("Authorization") String authHeader);
}
