package com.deodev.walletService.accountService.service;

import com.deodev.walletService.accountService.dto.response.CreateAccountResponse;
import com.deodev.walletService.accountService.dto.response.GetRecipientAccountUserDetailsResponse;
import com.deodev.walletService.accountService.dto.response.GetUserAccountsResponse;
import com.deodev.walletService.accountService.dto.response.request.ReserveFundsRequest;
import com.deodev.walletService.accountService.model.Account;
import com.deodev.walletService.accountService.repository.AccountRepository;
import com.deodev.walletService.client.UserServiceClient;
import com.deodev.walletService.dto.response.GetUserDetailsResponse;
import com.deodev.walletService.enums.Currency;
import com.deodev.walletService.exception.DuplicateAccountNumberException;
import com.deodev.walletService.exception.ExternalServiceException;
import com.deodev.walletService.accountService.dto.response.ReserveFundsResponse;
import com.deodev.walletService.exception.InsufficientBalanceException;
import com.deodev.walletService.walletService.model.Wallet;
import com.deodev.walletService.walletService.repository.WalletRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static com.deodev.walletService.util.AccountNumberGenerator.*;
import static com.deodev.walletService.util.AccountNumberValidatorUtil.*;
import static com.deodev.walletService.common.ErrorCodes.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final WalletRepository walletRepository;
    private final UserServiceClient userServiceClient;


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

    public GetRecipientAccountUserDetailsResponse findAccountAndUserDetails(String accountNumber, String jwt) {
        Account recipientAccount = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account number does not exist"));

        try {
            GetUserDetailsResponse userDetails = userServiceClient.getUser(
                    recipientAccount.getUserId().toString(),
                    "Bearer " + jwt);

            return GetRecipientAccountUserDetailsResponse.builder()
                    .username(userDetails.username())
                    .firstName(userDetails.firstName())
                    .lastName(userDetails.lastName())
                    .build();
        } catch (FeignException ex) {
            throw new ExternalServiceException(ex.getMessage(), ex.getCause());
        }
    }

    public GetUserAccountsResponse getUserAccounts(String userId) {
        List<Account> accounts = accountRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new IllegalArgumentException("Account(s) not found under user ID: " + userId));

        return GetUserAccountsResponse.builder()
                .accounts(accounts)
                .build();
    }

    public ReserveFundsResponse reserveFunds(ReserveFundsRequest request, String userId) {
        try {
            Account account = accountRepository.findByUserIdAndAccountNumber(UUID.fromString(userId), request.accountNumber())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Account not found for userId: %s".formatted(userId)
                    ));

            if (account.getBalance() < request.amount()) {
                throw new InsufficientBalanceException(
                        "Insufficient funds for userId: %s".formatted(userId)
                );
            }

            account.setBalance(account.getBalance() - request.amount());
            accountRepository.save(account);

            return ReserveFundsResponse.builder()
                    .isSuccess(true)
                    .build();

        } catch (InsufficientBalanceException e) {
            log.error(e.getMessage(), e);
            return ReserveFundsResponse.builder()
                    .isSuccess(false)
                    .errorCode(INSUFFICIENT_FUNDS)
                    .build();

        } catch (IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            return ReserveFundsResponse.builder()
                    .isSuccess(false)
                    .errorCode(NOT_FOUND)
                    .build();

        } catch (Exception e) {
            log.error("Unexpected error reserving funds, {}", e.getMessage(), e);
            return ReserveFundsResponse.builder()
                    .isSuccess(false)
                    .errorCode(SYSTEM_ERROR)
                    .build();
        }
    }

    private boolean accountNumberExists(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }

}
