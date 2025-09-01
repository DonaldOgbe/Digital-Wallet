package com.deodev.walletService.accountService.service;

import com.deodev.walletService.accountService.dto.CreateAccountResponse;
import com.deodev.walletService.accountService.dto.response.GetRecipientAccountUserDetailsResponse;
import com.deodev.walletService.accountService.model.Account;
import com.deodev.walletService.accountService.repository.AccountRepository;
import com.deodev.walletService.client.UserServiceClient;
import com.deodev.walletService.dto.response.GetUserDetailsResponse;
import com.deodev.walletService.enums.Currency;
import com.deodev.walletService.exception.DuplicateAccountNumberException;
import com.deodev.walletService.exception.ExternalServiceException;
import com.deodev.walletService.walletService.model.Wallet;
import com.deodev.walletService.walletService.repository.WalletRepository;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
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
    private UserServiceClient userServiceClient;

    @Test
    void createAccountAndSendResponse() {
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
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.currency()).isEqualTo(currency);
        assertThat(response.accountId()).isEqualTo(accountId);
        assertThat(response.accountNumber()).isEqualTo(accountNumber);
        assertThat(response.walletId()).isEqualTo(wallet.getId());
        assertThat(response.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());

        verify(walletRepository).findByUserId(userId);
        verify(accountRepository).existsByAccountNumber(any());
        verify(accountRepository).save(any());
    }

    @Test
    void throwErrorForDuplicateAccountNumber() {
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
    void throwErrorForRaceConditionDuplicateAccountNumber() {
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
        when(accountRepository.save(any())).thenThrow(DuplicateAccountNumberException.class);

        // then
        assertThrows(DuplicateAccountNumberException.class, () -> {
            accountService.createAccount(String.valueOf(userId), currency);
        });

        verify(walletRepository).findByUserId(userId);
        verify(accountRepository).existsByAccountNumber(any());
        verify(accountRepository).save(any());
    }

    @Test
    void getRecipientAccountUserDetailsAndSendResponse() {
        // given
        String accountNumber = "0123456789";
        String jwt = "jwt-token";

        Account account = Account.builder()
                .id(UUID.randomUUID())
                .accountNumber(accountNumber)
                .userId(UUID.randomUUID())
                .build();

        GetUserDetailsResponse userDetails = GetUserDetailsResponse.builder()
                .username("username")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        when(userServiceClient.getUser(account.getUserId().toString(), "Bearer " + jwt))
                .thenReturn(userDetails);

        // when
        GetRecipientAccountUserDetailsResponse actualResponse =
                accountService.findAccountAndUserDetails(accountNumber, jwt);

        // then
        assertThat(actualResponse.username()).isEqualTo("username");
        assertThat(actualResponse.firstName()).isEqualTo("John");
        assertThat(actualResponse.lastName()).isEqualTo("Doe");

        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(userServiceClient).getUser(account.getUserId().toString(), "Bearer " + jwt);
    }

    @Test
    void givenMissingAccount_whenFindAccountAndUserDetails_thenThrowIllegalArgument() {
        // given
        String accountNumber = "9999999999";
        String jwt = "jwt-token";
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.empty());

        // when + then
        assertThrows(IllegalArgumentException.class, () -> {
            accountService.findAccountAndUserDetails(accountNumber, jwt);
        });
    }

    @Test
    void givenFeignClientThrows_whenFindAccountAndUserDetails_thenThrowExternalServiceException() {
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

        when(userServiceClient.getUser(account.getUserId().toString(),"Bearer " + jwt))
                .thenThrow(FeignException.class);

        // when + then
        assertThrows(ExternalServiceException.class, () -> {
            accountService.findAccountAndUserDetails(accountNumber, jwt);
        });
    }
}