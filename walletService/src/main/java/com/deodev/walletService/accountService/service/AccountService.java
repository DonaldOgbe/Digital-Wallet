package com.deodev.walletService.accountService.service;

import com.deodev.walletService.accountService.dto.response.CreateAccountResponse;
import com.deodev.walletService.accountService.dto.response.GetRecipientAccountUserDetailsResponse;
import com.deodev.walletService.accountService.dto.response.GetUserAccountsResponse;
import com.deodev.walletService.accountService.dto.response.request.ReserveFundsRequest;
import com.deodev.walletService.accountService.model.Account;
import com.deodev.walletService.accountService.repository.AccountRepository;
import com.deodev.walletService.client.UserServiceClient;
import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.response.GetUserDetailsResponse;
import com.deodev.walletService.enums.Currency;
import com.deodev.walletService.exception.*;
import com.deodev.walletService.accountService.dto.response.ReserveFundsResponse;
import com.deodev.walletService.walletService.model.Wallet;
import com.deodev.walletService.walletService.repository.WalletRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static com.deodev.walletService.util.AccountNumberGenerator.*;

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
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for userId: %s".formatted(userId)));

            String accountNumber = generateAccountNumber();

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
                    .isSuccess(true)
                    .statusCode(HttpStatus.CREATED)
                    .timestamp(LocalDateTime.now())
                    .userId(userUuid)
                    .walletId(wallet.getId())
                    .accountId(saved.getId())
                    .accountNumber(saved.getAccountNumber())
                    .currency(saved.getCurrency())
                    .build();
    }

    public GetRecipientAccountUserDetailsResponse findAccountAndUserDetails(String accountNumber, String jwt) {
        Account recipientAccount = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account number does not exist"));

        try {
            ApiResponse<GetUserDetailsResponse> response = userServiceClient.getUser(
                    recipientAccount.getUserId().toString(),
                    "Bearer " + jwt);

            GetUserDetailsResponse userDetails = response.getData();

            return GetRecipientAccountUserDetailsResponse.builder()
                    .isSuccess(true)
                    .statusCode(HttpStatus.OK)
                    .timestamp(LocalDateTime.now())
                    .username(userDetails.username())
                    .firstName(userDetails.firstName())
                    .lastName(userDetails.lastName())
                    .build();
        } catch (Exception e) {
            throw new ExternalServiceException(e.getMessage(), e);
        }
    }

    public GetUserAccountsResponse getUserAccounts(String userId) {
        List<Account> accounts = accountRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Account(s) not found under user ID: " + userId));

        return GetUserAccountsResponse.builder()
                .isSuccess(true)
                .statusCode(HttpStatus.OK)
                .accounts(accounts)
                .build();
    }

    public ReserveFundsResponse reserveFunds(ReserveFundsRequest request, String userId) {
            Account account = accountRepository.findByUserIdAndAccountNumber(UUID.fromString(userId), request.accountNumber())
                    .orElseThrow(() -> new ResourceNotFoundException(
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
                    .status(HttpStatus.OK)
                    .timestamp(LocalDateTime.now())
                    .build();
    }

    private boolean accountNumberExists(String accountNumber) {
        return accountRepository.existsByAccountNumber(accountNumber);
    }

}
