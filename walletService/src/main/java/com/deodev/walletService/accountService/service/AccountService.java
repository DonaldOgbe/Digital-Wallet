package com.deodev.walletService.accountService.service;

import com.deodev.walletService.accountService.dto.response.*;
import com.deodev.walletService.accountService.dto.request.ReserveFundsRequest;
import com.deodev.walletService.accountService.dto.request.TransferFundsRequest;
import com.deodev.walletService.accountService.model.Account;
import com.deodev.walletService.accountService.model.FundReservation;
import com.deodev.walletService.accountService.repository.AccountRepository;
import com.deodev.walletService.client.UserServiceClient;
import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.response.GetUserDetailsResponse;
import com.deodev.walletService.enums.Currency;
import com.deodev.walletService.enums.FundReservationStatus;
import com.deodev.walletService.exception.*;
import com.deodev.walletService.walletPinService.service.WalletPinService;
import com.deodev.walletService.walletService.model.Wallet;
import com.deodev.walletService.walletService.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final WalletService walletService;
    private final FundReservationService fundReservationService;
    private final UserServiceClient userServiceClient;
    private final WalletPinService walletPinService;


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

    public GetRecipientAccountUserDetailsResponse findAccountAndUserDetails(String accountNumber, Currency currency) {
        Account recipientAccount = findByAccountNumberAndCurrency(accountNumber, currency);
        GetUserDetailsResponse userDetails = getUserDetailsFromClient(recipientAccount.getUserId());

        return GetRecipientAccountUserDetailsResponse.builder()
                .firstName(userDetails.firstName())
                .lastName(userDetails.lastName())
                .build();
    }

    public GetUserAccountsResponse getUserAccounts(String userId) {
        List<Account> accounts = accountRepository.findByUserId(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Account(s) not found under user ID: " + userId));

        return GetUserAccountsResponse.builder()
                .accounts(accounts)
                .build();
    }

    public ReserveFundsResponse validateAndReserveFunds(ReserveFundsRequest request, String userId) {
        walletPinService.validatePin(userId, request.pin());

        Account account = hasSufficientFunds(UUID.fromString(userId), request);

        FundReservation fundReservation = fundReservationService.createNewReservation(
                account.getAccountNumber(), request.transactionId(), request.amount());

        return ReserveFundsResponse.builder()
                .fundReservationId(fundReservation.getId())
                .build();
    }

    public TransferFundsResponse transferFunds(TransferFundsRequest request) {
        FundReservation reservation = fundReservationService.findByTransactionId(request.transactionId());
        isReservationValidForTransfer(reservation);

        debitSender(reservation.getAccountNumber(), reservation.getAmount());
        creditReceiver(request.accountNumber(), reservation.getAmount());

        fundReservationService.setUsedReservation(reservation);

        return TransferFundsResponse.builder()
                .transactionId(reservation.getTransactionId())
                .fundReservationId(reservation.getId())
                .build();
    }

    public ReleaseFundsResponse releaseFunds(UUID transactionId) {
        FundReservation reservation = fundReservationService.findByTransactionId(transactionId);

        isReservationValidForRelease(reservation);

        fundReservationService.setReleasedReservation(reservation);

        return ReleaseFundsResponse.builder()
                .transactionId(reservation.getTransactionId())
                .fundReservationId(reservation.getId())
                .build();
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

    void debitSender(String sender, Long amount) {
        try {
            debitBalance(sender, amount);
        } catch (Exception e) {
            throw new PeerToPeerTransferException("Failed to debit sender account: " + sender, e);
        }
    }

    void creditReceiver(String receiver, Long amount) {
        try {
            creditBalance(receiver, amount);
        } catch (Exception e) {
            throw new PeerToPeerTransferException("Failed to credit receiver account: " + receiver, e);
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

    GetUserDetailsResponse getUserDetailsFromClient(UUID userId) {
        try {
            ApiResponse<GetUserDetailsResponse> response = userServiceClient.getUser(
                    userId);

            return response.getData();
        } catch (Exception e) {
            throw new ExternalServiceException(e.getMessage(), e);
        }
    }

    Account hasSufficientFunds(UUID userId, ReserveFundsRequest request) {
        Account account = accountRepository.findByUserIdAndAccountNumber(userId, request.accountNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found for userId: %s".formatted(userId)
                ));

        if (account.getBalance() < request.amount()) {
            throw new InsufficientBalanceException(
                    "Insufficient funds for userId: %s".formatted(userId)
            );
        }

        return account;
    }

    void isReservationValidForTransfer(FundReservation reservation) {
        if (reservation.getExpiredAt() != null && reservation.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new FundReservationException("Fund reservation has expired");
        }

        if (reservation.getStatus() != FundReservationStatus.ACTIVE) {
            throw new FundReservationException("Fund reservation is not active");
        }
    }

    void isReservationValidForRelease(FundReservation reservation) {
        if (reservation.getStatus() == FundReservationStatus.USED) {
            throw new FundReservationException("Reservation already used");
        }
        if (reservation.getStatus() == FundReservationStatus.RELEASED) {
            throw new FundReservationException("Reservation already released");
        }
    }

}
