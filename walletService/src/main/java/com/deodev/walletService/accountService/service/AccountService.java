package com.deodev.walletService.accountService.service;

import com.deodev.walletService.accountService.dto.request.AccountExistsRequest;
import com.deodev.walletService.accountService.dto.request.P2PTransferRequest;
import com.deodev.walletService.accountService.dto.response.*;
import com.deodev.walletService.accountService.model.Account;
import com.deodev.walletService.accountService.model.FundReservation;
import com.deodev.walletService.accountService.repository.AccountRepository;
import com.deodev.walletService.client.UserServiceClient;
import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.response.GetUserDetailsResponse;
import com.deodev.walletService.enums.Currency;
import com.deodev.walletService.enums.FundReservationStatus;
import com.deodev.walletService.exception.*;
import com.deodev.walletService.rabbitmq.publisher.WalletEventsPublisher;
import com.deodev.walletService.walletPinService.service.WalletPinService;
import com.deodev.walletService.walletService.model.Wallet;
import com.deodev.walletService.walletService.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.deodev.walletService.util.AccountNumberGenerator.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final WalletService walletService;
    private final FundReservationService fundReservationService;
    private final UserServiceClient userServiceClient;
    private final WalletPinService walletPinService;
    private final ObjectMapper mapper;

    public CreateAccountResponse createAccount(String userId, Currency currency) {

        Wallet wallet = walletService.findByUserId(UUID.fromString(userId));
        String accountNumber = generateAccountNumber();
        verifyAccountNumber(accountNumber);

        Account account = Account.builder()
                .walletId(wallet.getId())
                .userId(wallet.getUserId())
                .accountNumber(accountNumber)
                .currency(currency)
                .build();

        Account savedAccount = saveAccount(account);

        return CreateAccountResponse.builder()
                .userId(savedAccount.getUserId())
                .walletId(savedAccount.getWalletId())
                .accountId(savedAccount.getId())
                .accountNumber(savedAccount.getAccountNumber())
                .currency(savedAccount.getCurrency())
                .build();
    }

    public ApiResponse<?> findAccountAndUserDetails(String accountNumber, Currency currency) {
        Account recipientAccount = findByAccountNumberAndCurrency(accountNumber, currency);

        ApiResponse<?> response = getUserDetailsFromClient(recipientAccount.getUserId());

        if (!response.isSuccess()) {
            return response;
        }

        GetUserDetailsResponse userDetails = mapper.convertValue(response.getData(), GetUserDetailsResponse.class);

        return ApiResponse.success(HttpStatus.OK.value(), userDetails);
    }

    public GetUserAccountsResponse getUserAccounts(String userId) {
        List<Account> accounts = accountRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Account(s) not found under user ID: " + userId));

        return GetUserAccountsResponse.builder()
                .accounts(accounts)
                .build();
    }

    @Transactional
    public ApiResponse<?> P2PTransfer(P2PTransferRequest request, String userId) {
        try {
            walletPinService.validatePin(userId, request.pin());

            FundReservation reservation = reserveFunds(UUID.fromString(userId), request);

            debitSender(request.senderAccountNumber(), request.amount());
            creditReceiver(request.receiverAccountNumber(), request.amount());

            fundReservationService.setUsedReservation(reservation);

            return ApiResponse.success(HttpStatus.OK.value(), "success");
        } catch (Exception e) {
            log.error("P2P Transfer failed", e);
            throw e;
        }
    }

    public void creditBalance(String accountNumber, long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account number does not exist"));

        account.setBalance(account.getBalance() + amount);
        accountRepository.save(account);
    }

    public void debitBalance(String accountNumber, long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account number does not exist"));

        account.setBalance(account.getBalance() - amount);
        accountRepository.save(account);
    }

    public ApiResponse<?> accountExists(AccountExistsRequest request) {
        try {
            findByAccountNumberAndCurrency(request.accountNumber(), request.currency());
        } catch (ResourceNotFoundException ex) {
            log.warn("Account Number not found, account number: {}, currency: {}", request.accountNumber(), request.currency());
            return ApiResponse.success(HttpStatus.OK.value(), false);
        }
        return ApiResponse.success(HttpStatus.OK.value(), true);
    }

    FundReservation reserveFunds(UUID userId, P2PTransferRequest request) {
        Account account = hasSufficientFunds(userId, request.senderAccountNumber(), request.amount());

        return fundReservationService.createNewReservation(
                account.getAccountNumber(), request.transactionId(), request.amount());
    }

    void debitSender(String sender, Long amount) {
        try {
            debitBalance(sender, amount);
        } catch (Exception e) {
            throw new P2PTransferException("Failed to debit sender account: " + sender, e);
        }
    }

    void creditReceiver(String receiver, Long amount) {
        try {
            creditBalance(receiver, amount);
        } catch (Exception e) {
            throw new P2PTransferException("Failed to credit receiver account: " + receiver, e);
        }
    }

    void verifyAccountNumber(String accountNumber) {
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new DuplicateAccountNumberException("Account number already exists: %s".formatted(accountNumber));
        }
    }

    Account saveAccount(Account account) {
        try {
            return accountRepository.save(account);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateAccountNumberException("Account number already exists: %s".formatted(account.getAccountNumber()));
        }
    }

    Account findByAccountNumberAndCurrency(String accountNumber, Currency currency) {
        return accountRepository.findByAccountNumberAndCurrency(accountNumber, currency)
                .orElseThrow(() -> new ResourceNotFoundException("Account number does not exist"));
    }

    ApiResponse<?> getUserDetailsFromClient(UUID userId) {
        try {
            return userServiceClient.getUser(userId).getBody();
        } catch (Exception e) {
            throw new ExternalServiceException(e.getMessage(), e);
        }
    }

    Account hasSufficientFunds(UUID userId, String accountNumber, Long amount) {
        Account account = accountRepository.findByUserIdAndAccountNumber(userId, accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found for userId: %s".formatted(userId)
                ));

        if (account.getBalance() < amount) {
            throw new InsufficientBalanceException(
                    "Insufficient funds for userId: %s".formatted(userId)
            );
        }

        return account;
    }

}
