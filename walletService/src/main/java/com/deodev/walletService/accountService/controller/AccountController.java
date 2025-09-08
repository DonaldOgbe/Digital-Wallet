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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/wallets/accounts")
public class AccountController {

    private final AccountService accountService;

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/{currency}")
    public ResponseEntity<?> createAccount(@PathVariable Currency currency,
                                           @RequestAttribute("userId") String userId) {

        CreateAccountResponse response = accountService.createAccount(userId, currency);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(HttpStatus.CREATED.value(), response));
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping("/recipient")
    public ResponseEntity<?> getRecipientDetails(@RequestParam String accountNumber,
                                                 @RequestHeader("Authorization") String jwt) {
        GetRecipientAccountUserDetailsResponse response = accountService.findAccountAndUserDetails(accountNumber, jwt);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(HttpStatus.OK.value(), response));
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @GetMapping
    public ResponseEntity<?> getUserAccounts(@RequestAttribute("userId") String userId) {
        GetUserAccountsResponse response = accountService.getUserAccounts(userId);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(HttpStatus.OK.value(), response)
        );
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/funds/reserve")
    public ResponseEntity<?> reserveFunds(@Valid @RequestBody ReserveFundsRequest request,
                                          @RequestAttribute("userId") String userId) {
        ReserveFundsResponse response = accountService.reserveFunds(request, userId);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(HttpStatus.OK.value(), response)
        );
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/funds/transfer")
    public ResponseEntity<?> transferFunds(@Valid  @RequestBody TransferFundsRequest request) {
        TransferFundsResponse response = accountService.transferFunds(request);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(HttpStatus.OK.value(), response)
        );
    }
}
