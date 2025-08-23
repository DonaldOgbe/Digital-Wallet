package com.deodev.walletService.accountService.controller;

import com.deodev.walletService.accountService.dto.CreateAccountResponse;
import com.deodev.walletService.accountService.service.AccountService;
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
    public ResponseEntity<?> createAccount(@Valid
                                           @PathVariable Currency currency,
                                           @RequestAttribute("userId") String userId) {

        CreateAccountResponse response = accountService.createAccount(userId, currency);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
