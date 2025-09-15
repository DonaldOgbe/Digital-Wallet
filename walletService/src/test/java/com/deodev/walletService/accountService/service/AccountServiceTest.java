package com.deodev.walletService.accountService.service;

import com.deodev.walletService.accountService.dto.request.TransferFundsRequest;
import com.deodev.walletService.accountService.dto.response.*;
import com.deodev.walletService.accountService.dto.request.ReserveFundsRequest;
import com.deodev.walletService.accountService.model.Account;
import com.deodev.walletService.accountService.model.FundReservation;
import com.deodev.walletService.accountService.repository.AccountRepository;
import com.deodev.walletService.accountService.repository.FundReservationRepository;
import com.deodev.walletService.client.UserServiceClient;
import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.response.GetUserDetailsResponse;
import com.deodev.walletService.enums.Currency;
import com.deodev.walletService.enums.FundReservationStatus;
import com.deodev.walletService.exception.*;
import com.deodev.walletService.walletService.model.Wallet;
import com.deodev.walletService.walletService.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    private AccountService accountService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private FundReservationRepository fundReservationRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Nested
    class createAccount {
        @Test
        void createAccount_ReturnSuccessResponse() {
            // given
            UUID userId = UUID.randomUUID();
            Currency currency = Currency.NGN;

            Wallet wallet = Wallet.builder()
                    .id(UUID.randomUUID())
                    .build();

            UUID accountId = UUID.randomUUID();
            String accountNumber = "0123456789";


            // when
            when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
            when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
            when(accountRepository.save(any())).thenAnswer(
                    invocationOnMock -> {
                        Account account = invocationOnMock.getArgument(0);
                        account.setAccountNumber(accountNumber);
                        account.setId(accountId);

                        return account;
                    }
            );

            CreateAccountResponse response = accountService.createAccount(String.valueOf(userId), currency);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.currency()).isEqualTo(currency);
            assertThat(response.accountId()).isEqualTo(accountId);
            assertThat(response.accountNumber()).isEqualTo(accountNumber);
            assertThat(response.walletId()).isEqualTo(wallet.getId());
            assertThat(response.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
            verify(accountRepository).save(any());
        }

        @Test
        void createAccount_ThrowsResourceNotFound() {
            // given
            UUID userId = UUID.randomUUID();

            when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // when + then
            assertThrows(ResourceNotFoundException.class, () -> {
                accountService.createAccount(String.valueOf(userId), Currency.NGN);
            });

            verify(walletRepository).findByUserId(userId);
        }

        @Test
        void createAccount_ThrowsDuplicateAccountNumber_ForExistingAccount() {
            // given
            UUID userId = UUID.randomUUID();

            Wallet wallet = Wallet.builder()
                    .id(UUID.randomUUID())
                    .build();

            String accountNumber = "0123456789";


            // when
            when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
            when(accountRepository.existsByAccountNumber(any())).thenReturn(true);


            // then
            assertThrows(DuplicateAccountNumberException.class, () -> {
                accountService.createAccount(String.valueOf(userId), Currency.NGN);
            });

            verify(walletRepository).findByUserId(userId);
            verify(accountRepository).existsByAccountNumber(any());
        }

        @Test
        void createAccount_ThrowsDuplicateAccountNumber_RaceCondition() {
            // given
            UUID userId = UUID.randomUUID();

            Wallet wallet = Wallet.builder()
                    .id(UUID.randomUUID())
                    .build();

            UUID accountId = UUID.randomUUID();
            String accountNumber = "0123456789";


            // when
            when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
            when(accountRepository.existsByAccountNumber(any())).thenReturn(false);
            when(accountRepository.save(any())).thenThrow(DuplicateAccountNumberException.class);

            // then
            assertThrows(DuplicateAccountNumberException.class, () -> {
                accountService.createAccount(String.valueOf(userId), Currency.NGN);
            });

            verify(walletRepository).findByUserId(userId);
            verify(accountRepository).existsByAccountNumber(any());
            verify(accountRepository).save(any());
        }
    }

    @Nested
    class findRecipientAccountUserDetails {
        @Test
        void findRecipientAccountUserDetails_ReturnSuccessResponse() {
            // given
            String accountNumber = "0123456789";
            String jwt = "jwt-token";

            Account account = Account.builder()
                    .id(UUID.randomUUID())
                    .accountNumber(accountNumber)
                    .userId(UUID.randomUUID())
                    .build();

            ApiResponse<GetUserDetailsResponse> userDetails = ApiResponse.success(
                    HttpStatus.OK.value(),
                    GetUserDetailsResponse.builder()
                            .username("username")
                            .firstName("John")
                            .lastName("Doe")
                            .build());

            when(accountRepository.findByAccountNumber(accountNumber))
                    .thenReturn(Optional.of(account));

            when(userServiceClient.getUser(account.getUserId().toString(), "Bearer " + jwt))
                    .thenReturn(userDetails);

            // when
            GetRecipientAccountUserDetailsResponse response =
                    accountService.findAccountAndUserDetails(accountNumber, jwt);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(response.username()).isEqualTo("username");
            assertThat(response.firstName()).isEqualTo("John");
            assertThat(response.lastName()).isEqualTo("Doe");

            verify(accountRepository).findByAccountNumber(accountNumber);
            verify(userServiceClient).getUser(account.getUserId().toString(), "Bearer " + jwt);
        }

        @Test
        void findRecipientAccountUserDetails_ThrowsResourceNotFound() {
            // given
            String accountNumber = "9999999999";
            String jwt = "jwt-token";
            when(accountRepository.findByAccountNumber(accountNumber))
                    .thenReturn(Optional.empty());

            // when + then
            assertThrows(ResourceNotFoundException.class, () -> {
                accountService.findAccountAndUserDetails(accountNumber, jwt);
            });
        }

        @Test
        void findRecipientAccountUserDetails_ThrowsExternalServiceException() {
            // given
            String accountNumber = "0123456789";
            String jwt = "jwt-token";
            Account account = Account.builder()
                    .id(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .accountNumber(accountNumber)
                    .build();

            when(accountRepository.findByAccountNumber(accountNumber))
                    .thenReturn(Optional.of(account));

            when(userServiceClient.getUser(account.getUserId().toString(), "Bearer " + jwt))
                    .thenThrow(ExternalServiceException.class);

            // when + then
            assertThrows(ExternalServiceException.class, () -> {
                accountService.findAccountAndUserDetails(accountNumber, jwt);
            });
        }
    }

    @Nested
    class getUserAccounts {
        @Test
        void getUserAccounts_ReturnsListOfAccounts() {
            // given
            UUID userId = UUID.randomUUID();
            Account account1 = Account.builder()
                    .id(UUID.randomUUID())
                    .balance(10000L)
                    .accountNumber("1234567890").build();

            Account account2 = Account.builder()
                    .id(UUID.randomUUID())
                    .balance(10000L)
                    .accountNumber("9876543210").build();

            List<Account> accounts = List.of(account1, account2);

            when(accountRepository.findByUserId(userId)).thenReturn(Optional.of(accounts));

            // when
            GetUserAccountsResponse response = accountService.getUserAccounts(userId.toString());

            // then
            assertThat(response).isNotNull();
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.accounts()).hasSize(2);

            verify(accountRepository).findByUserId(any());
        }

        @Test
        void getUserAccounts_ThrowsResourceNotFound() {
            // given
            UUID userId = UUID.randomUUID();

            when(accountRepository.findByUserId(userId)).thenReturn(Optional.empty());

            // when + then
            assertThrows(ResourceNotFoundException.class, () -> {
                accountService.getUserAccounts(userId.toString());
            });
        }
    }

    @Nested
    class reserveFunds {
        @Test
        void reserveFunds_ReturnsSuccess() {
            // given
            UUID userId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            UUID fundReservationId = UUID.randomUUID();
            String accountNumber = "0123456789";

            Account account = Account.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .accountNumber(accountNumber)
                    .balance(10000L)
                    .build();

            ReserveFundsRequest request = ReserveFundsRequest.builder()
                    .accountNumber(accountNumber)
                    .amount(1000L)
                    .transactionId(transactionId)
                    .build();

            when(accountRepository.findByUserIdAndAccountNumber(userId, accountNumber)).thenReturn(Optional.of(account));
            when(fundReservationRepository.save(any(FundReservation.class))).thenAnswer(invocationOnMock -> {
                FundReservation fundReservation = invocationOnMock.getArgument(0);
                fundReservation.setId(fundReservationId);
                return fundReservation;
            });

            // when
            ReserveFundsResponse response = accountService.reserveFunds(request, String.valueOf(userId));

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(response.fundReservationId()).isEqualTo(fundReservationId);
            verify(accountRepository).findByUserIdAndAccountNumber(userId, accountNumber);
            verify(fundReservationRepository).save(any(FundReservation.class));
        }

        @Test
        void reserveFunds_ThrowsInsufficientFunds() {
            // given
            UUID userId = UUID.randomUUID();
            String accountNumber = "0123456789";

            Account account = Account.builder()
                    .userId(userId)
                    .accountNumber(accountNumber)
                    .balance(500L)
                    .build();

            ReserveFundsRequest request = ReserveFundsRequest.builder()
                    .accountNumber(accountNumber)
                    .amount(1000L)
                    .build();

            when(accountRepository.findByUserIdAndAccountNumber(userId, accountNumber))
                    .thenReturn(Optional.of(account));

            // when + then
            assertThrows(InsufficientBalanceException.class, () -> {
                accountService.reserveFunds(request, String.valueOf(userId));
            });

            verify(accountRepository).findByUserIdAndAccountNumber(userId, accountNumber);
        }

        @Test
        void reserveFunds_ThrowsResourceNotFound() {
            // given
            UUID userId = UUID.randomUUID();
            String accountNumber = "0123456789";

            ReserveFundsRequest request = ReserveFundsRequest.builder()
                    .accountNumber(accountNumber)
                    .amount(1000L)
                    .build();

            when(accountRepository.findByUserIdAndAccountNumber(userId, accountNumber))
                    .thenReturn(Optional.empty());

            // when + then
            assertThrows(ResourceNotFoundException.class, () -> {
                accountService.reserveFunds(request, String.valueOf(userId));
            });

            verify(accountRepository).findByUserIdAndAccountNumber(userId, accountNumber);
        }
    }

    @Nested
    class transferFunds {
        FundReservation reservation;
        UUID senderId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        UUID reservationId = UUID.randomUUID();
        String senderAccountNumber = "1111111111";
        String receiverAccountNumber = "2222222222";
        TransferFundsRequest request;

        @BeforeEach
        void setup() {
            reservation = FundReservation.builder()
                    .id(reservationId)
                    .accountNumber(senderAccountNumber)
                    .transactionId(transactionId)
                    .amount(200L)
                    .status(FundReservationStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .expiredAt(LocalDateTime.now().plusHours(1))
                    .build();

            request = TransferFundsRequest.builder()
                    .accountNumber(receiverAccountNumber)
                    .transactionId(transactionId)
                    .build();
        }

        @Test
        void transferFunds_ReturnsSuccess() {
            // given
            Account sender = Account.builder()
                    .id(senderId)
                    .walletId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .accountNumber(senderAccountNumber)
                    .currency(Currency.NGN)
                    .balance(1000L)
                    .build();

            Account receiver = Account.builder()
                    .id(receiverId)
                    .walletId(UUID.randomUUID())
                    .userId(UUID.randomUUID())
                    .accountNumber(receiverAccountNumber)
                    .currency(Currency.NGN)
                    .balance(1000L)
                    .build();

            when(fundReservationRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(reservation));
            when(accountRepository.findByAccountNumber(sender.getAccountNumber()))
                    .thenReturn(Optional.of(sender));
            when(accountRepository.findByAccountNumber(receiver.getAccountNumber()))
                    .thenReturn(Optional.of(receiver));

            // when
            TransferFundsResponse response = accountService.transferFunds(request);

            // then
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(response.transactionId()).isEqualTo(transactionId);
            assertThat(response.fundReservationId()).isEqualTo(reservationId);

            verify(fundReservationRepository).findByTransactionId(transactionId);
            verify(accountRepository).findByAccountNumber(sender.getAccountNumber());
            verify(accountRepository).findByAccountNumber(receiver.getAccountNumber());
            verify(fundReservationRepository).save(reservation);
        }

        @Test
        void transferFunds_ThrowsResourceNotFound() {
            // given
            when(fundReservationRepository.findByTransactionId(transactionId))
                    .thenReturn(Optional.empty());

            // when + then
            assertThrows(ResourceNotFoundException.class, () ->
                    accountService.transferFunds(request));

            verify(fundReservationRepository).findByTransactionId(transactionId);
        }

        @Test
        void transferFunds_ThrowsFundReservationException_ForExpiredReservation() {
            // given
            reservation.setExpiredAt(LocalDateTime.now().minusMinutes(1));

            when(fundReservationRepository.findByTransactionId(transactionId))
                    .thenReturn(Optional.of(reservation));

            // when + then
            assertThrows(FundReservationException.class, () ->
                    accountService.transferFunds(request));

            verify(fundReservationRepository).findByTransactionId(transactionId);
        }

        @Test
        void transferFunds_ThrowsFundReservationException_ForInactiveReservation() {
            // given
            reservation.setStatus(FundReservationStatus.USED);

            when(fundReservationRepository.findByTransactionId(transactionId))
                    .thenReturn(Optional.of(reservation));

            // when + then
            assertThrows(FundReservationException.class, () ->
                    accountService.transferFunds(request));

            verify(fundReservationRepository).findByTransactionId(transactionId);
        }
    }

}