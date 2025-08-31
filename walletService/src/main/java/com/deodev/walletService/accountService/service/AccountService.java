package com.deodev.walletService.accountService.service;

import com.deodev.walletService.accountService.dto.CreateAccountResponse;
import com.deodev.walletService.accountService.dto.response.GetRecipientAccountUserDetailsResponse;
import com.deodev.walletService.accountService.model.Account;
import com.deodev.walletService.accountService.repository.AccountRepository;
import com.deodev.walletService.enums.Currency;
import com.deodev.walletService.exception.DuplicateAccountNumberException;
import com.deodev.walletService.walletService.model.Wallet;
import com.deodev.walletService.walletService.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import static com.deodev.walletService.util.AccountNumberGenerator.*;
import static com.deodev.walletService.util.AccountNumberValidatorUtil.*;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;


    public CreateAccountResponse createAccount(String userId, Currency currency) {

        UUID userUuid = UUID.fromString(userId);
        Wallet wallet = walletRepository.findByUserId(userUuid)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found for userId: %s".formatted(userId)));

        String accountNumber = generateAccountNumber();

        if (!validateAccountNumber(accountNumber)) {
            throw new IllegalArgumentException("Generated account number is invalid: %s".formatted(accountNumber));
        }

        if (accountNumberExists(accountNumber)) {
            throw new DuplicateAccountNumberException("Account number already exists: %s".formatted(accountNumber));
        }

        Account account = Account.builder()
                .walletId(wallet.getId())
                .userId(userUuid)
                .accountNumber(accountNumber)
                .currency(currency)
                .build();

        Account saved;

        try {
            saved = accountRepository.save(account);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateAccountNumberException("Account number already exists: %s".formatted(account.getAccountNumber()));
        }

        return CreateAccountResponse.builder()
                .userId(userUuid)
                .walletId(wallet.getId())
                .accountId(saved.getId())
                .accountNumber(saved.getAccountNumber())
                .currency(saved.getCurrency())
                .timestamp(LocalDateTime.now())
                .build();
    }

//    public GetRecipientAccountUserDetailsResponse findAccountAndUserDetails() {
//
//    }
    private boolean accountNumberExists(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }

}
