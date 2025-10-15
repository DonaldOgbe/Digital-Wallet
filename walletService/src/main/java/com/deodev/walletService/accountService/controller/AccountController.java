package com.deodev.walletService.accountService.controller;

import com.deodev.walletService.accountService.dto.request.AccountExistsRequest;
import com.deodev.walletService.accountService.dto.request.P2PTransferRequest;
import com.deodev.walletService.accountService.dto.response.*;
import com.deodev.walletService.accountService.service.AccountService;
import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.enums.Currency;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestParam Currency currency,
                                           @RequestHeader("X-User-Id") String userId) {

        CreateAccountResponse response = accountService.createAccount(userId, currency);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(HttpStatus.CREATED.value(), response));
    }

    @GetMapping("/recipient/{accountNumber}")
    public ResponseEntity<ApiResponse<?>> getRecipientDetails(@PathVariable String accountNumber,
                                                 @RequestParam Currency currency) {
        ApiResponse<?> response = accountService.findAccountAndUserDetails(accountNumber, currency);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verifyAccountNumber(@Valid @RequestBody AccountExistsRequest request) {
        ApiResponse<?> response = accountService.accountExists(request);
        return ResponseEntity.status((response.getStatusCode())).body(response);
    }


    @GetMapping
    public ResponseEntity<?> getUserAccounts(@RequestHeader("X-User-Id") String userId) {
        GetUserAccountsResponse response = accountService.getUserAccounts(userId);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(HttpStatus.OK.value(), response)
        );
    }

    @PostMapping("/funds/transfer/p2p")
    public ResponseEntity<ApiResponse<?>> p2pTransfer(@Valid @RequestBody P2PTransferRequest request,
                                          @RequestHeader("X-User-Id") String userId) {
        ApiResponse<?> response = accountService.P2PTransfer(request, userId);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
