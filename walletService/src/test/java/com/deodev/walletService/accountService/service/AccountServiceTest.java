package com.deodev.walletService.accountService.service;

import com.deodev.walletService.accountService.model.Account;
import com.deodev.walletService.accountService.repository.AccountRepository;
import com.deodev.walletService.client.UserServiceClient;
import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.response.GetUserDetailsResponse;
import com.deodev.walletService.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private AccountService accountService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void getUserDetailsFromClient_ShouldReturnResponse_WhenClientSucceeds() {
        // given
        GetUserDetailsResponse mockResponse = new GetUserDetailsResponse("John", "Doe", "johndoe@email.com");
        ApiResponse<GetUserDetailsResponse> apiResponse =
                ApiResponse.success(200, mockResponse);

        when(userServiceClient.getUser(userId))
                .thenReturn(apiResponse);

        // when
        GetUserDetailsResponse result = accountService.getUserDetailsFromClient(userId);

        // then
        assertEquals(mockResponse, result);
    }

    @Test
    void hasSufficientFunds_ShouldThrowResourceNotFound_WhenAccountDoesNotExist() {
        // given
        String accountNumber = "0123456789";
        Long amount = 100L;

        when(accountRepository.findByUserIdAndAccountNumber(userId, accountNumber))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(ResourceNotFoundException.class,
                () -> accountService.hasSufficientFunds(userId, accountNumber, amount));
    }

    @Test
    void hasSufficientFunds_ShouldThrowInsufficientBalance_WhenBalanceIsTooLow() {
        // given
        String accountNumber = "0123456789";
        Long amount = 100L;

        Account account = Account.builder()
                .balance(50L)
                .build();

        when(accountRepository.findByUserIdAndAccountNumber(userId, accountNumber))
                .thenReturn(Optional.of(account));

        // when & then
        assertThrows(InsufficientBalanceException.class,
                () -> accountService.hasSufficientFunds(userId, accountNumber, amount));
    }


}