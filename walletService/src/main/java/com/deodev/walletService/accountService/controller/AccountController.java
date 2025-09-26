package com.deodev.walletService.accountService.controller;

import com.deodev.walletService.accountService.dto.request.TransferFundsRequest;
import com.deodev.walletService.accountService.dto.response.*;
import com.deodev.walletService.accountService.dto.request.ReserveFundsRequest;
import com.deodev.walletService.accountService.service.AccountService;
import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.enums.Currency;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/{currency}")
    public ResponseEntity<?> createAccount(@PathVariable Currency currency,
                                           @RequestHeader("X-User-Id") String userId) {

        CreateAccountResponse response = accountService.createAccount(userId, currency);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(HttpStatus.CREATED.value(), response));
    }

    @GetMapping("/recipient/{accountNumber}")
    public ResponseEntity<?> getRecipientDetails(@PathVariable String accountNumber,
                                                 @RequestParam Currency currency) {
        GetRecipientAccountUserDetailsResponse response = accountService.findAccountAndUserDetails(accountNumber, currency);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @GetMapping
    public ResponseEntity<?> getUserAccounts(@RequestHeader("X-User-Id") String userId) {
        GetUserAccountsResponse response = accountService.getUserAccounts(userId);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(HttpStatus.OK.value(), response)
        );
    }

    @PostMapping("/funds/reserve")
    public ResponseEntity<?> reserveFunds(@Valid @RequestBody ReserveFundsRequest request,
                                          @RequestHeader("X-User-Id") String userId) {
        ReserveFundsResponse response = accountService.validateAndReserveFunds(request, userId);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(HttpStatus.OK.value(), response)
        );
    }

//    @PostMapping("/funds/transfer")
//    public ResponseEntity<?> transferFunds(@Valid @RequestBody TransferFundsRequest request) {
//        TransferFundsResponse response = accountService.transferFunds(request);
//
//        return ResponseEntity.status(HttpStatus.OK).body(
//                ApiResponse.success(HttpStatus.OK.value(), response)
//        );
//    }

    @PostMapping("/funds/{transactionId}/release")
    public ResponseEntity<?> releaseFunds(@PathVariable UUID transactionId) {
        ReleaseFundsResponse response = accountService.releaseFunds(transactionId);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(HttpStatus.OK.value(), response)
        );
    }
}
