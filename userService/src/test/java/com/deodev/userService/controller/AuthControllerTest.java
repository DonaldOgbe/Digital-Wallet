package com.deodev.userService.controller;

import com.deodev.userService.client.WalletServiceClient;
import com.deodev.userService.dto.request.CreateWalletRequest;
import com.deodev.userService.dto.request.UserRegistrationRequest;
import com.deodev.userService.dto.response.ApiResponse;
import com.deodev.userService.dto.response.CreateWalletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private WalletServiceClient mockWalletServiceClient;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("register new user and respond with Ok")
    void testRegisterUser() {

        // given
        UserRegistrationRequest requestBody = UserRegistrationRequest.builder()
                .email("test@email.com")
                .username("testname")
                .password("testPassword").build();

        HttpEntity requestEntity = new HttpEntity(requestBody);

        when(mockWalletServiceClient.createWallet(any(CreateWalletRequest.class), any(String.class))).thenReturn(CreateWalletResponse
                .builder()
                .success(true)
                .note("Wallet Created Successfully")
                .userId(UUID.randomUUID())
                .walletId(UUID.randomUUID())
                .build());

        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
                "/api/v1/auth/register",
                requestEntity,
                ApiResponse.class
        );

        assertTrue(response.getBody().isSuccess());
    }
}