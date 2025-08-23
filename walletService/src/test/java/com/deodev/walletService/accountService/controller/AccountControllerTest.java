package com.deodev.walletService.accountService.controller;

import com.deodev.walletService.accountService.dto.CreateAccountResponse;
import com.deodev.walletService.enums.Currency;
import com.deodev.walletService.util.JwtUtil;
import com.deodev.walletService.walletService.dto.request.CreateWalletRequest;
import com.deodev.walletService.walletService.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WalletService walletService;


    private HttpHeaders headers = new HttpHeaders();
    private String jwt;


    @Test
    void createAccountAndSend201Response() {
        // given
        UUID userId = UUID.randomUUID();
        walletService.createWallet(CreateWalletRequest.builder()
                .userId(userId)
                .build());

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("authorities", List.of("ROLE_USER"));
        extraClaims.put("userId", userId);

        jwt = jwtUtil.generateToken(extraClaims, "subject");
        headers.set("Authorization", "Bearer %s".formatted(jwt));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // when
        ResponseEntity<CreateAccountResponse> response = testRestTemplate.exchange(
                "/api/v1/wallets/accounts/{currency}",
                HttpMethod.POST,
                requestEntity,
                CreateAccountResponse.class,
                Currency.NGN
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        CreateAccountResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.userId()).isEqualTo(userId);
        assertThat(body.currency()).isEqualTo(Currency.NGN);
        assertThat(body.accountId()).isNotNull();
        assertThat(body.walletId()).isNotNull();
        assertThat(body.accountNumber()).isNotBlank();
        assertThat(body.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }



}