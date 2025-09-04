package com.deodev.walletService.accountService.controller;

import com.deodev.walletService.accountService.dto.response.CreateAccountResponse;
import com.deodev.walletService.accountService.dto.response.GetRecipientAccountUserDetailsResponse;
import com.deodev.walletService.accountService.dto.response.GetUserAccountsResponse;
import com.deodev.walletService.accountService.service.AccountService;
import com.deodev.walletService.client.UserServiceClient;
import com.deodev.walletService.dto.response.GetUserDetailsResponse;
import com.deodev.walletService.enums.Currency;
import com.deodev.walletService.util.JwtUtil;
import com.deodev.walletService.walletService.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WalletService walletService;

    @Autowired
    private AccountService accountService;

    @MockBean
    private UserServiceClient userServiceClient;


    private HttpHeaders headers;
    private String jwt;
    private Map<String, Object> extraClaims;

    @BeforeEach
    void setup() {
        headers = new HttpHeaders();
        extraClaims = new HashMap<>();
    }


    @Test
    void createAccountAndSend201Response() {
        // given
        UUID userId = UUID.randomUUID();
        walletService.createWallet(String.valueOf(userId));

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

    @Test
    void getRecipientDetailsAndSend200Response() {
        // given
        UUID userId = UUID.randomUUID();

        walletService.createWallet(userId.toString());
        CreateAccountResponse createdAccount = accountService.createAccount(userId.toString(), Currency.NGN);
        String recipientAccountNumber = createdAccount.accountNumber();

        GetUserDetailsResponse mockedUser = GetUserDetailsResponse.builder()
                .username("recipientUsername")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userServiceClient.getUser(any(), any())).thenReturn(mockedUser);

        // generate jwt
        extraClaims.put("authorities", List.of("ROLE_USER"));
        extraClaims.put("userId", userId);
        jwt = jwtUtil.generateToken(extraClaims, "subject");

        headers.set("Authorization", "Bearer %s".formatted(jwt));
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // when
        ResponseEntity<GetRecipientAccountUserDetailsResponse> response = testRestTemplate.exchange(
                "/api/v1/wallets/accounts/recipient?accountNumber={accountNumber}",
                HttpMethod.GET,
                requestEntity,
                GetRecipientAccountUserDetailsResponse.class,
                recipientAccountNumber
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        GetRecipientAccountUserDetailsResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.username()).isEqualTo("recipientUsername");
        assertThat(body.firstName()).isEqualTo("John");
        assertThat(body.lastName()).isEqualTo("Doe");
    }

    @Test
    void getUserAccountsAndSend200Response() {
        // given
        UUID userId = UUID.randomUUID();
        walletService.createWallet(String.valueOf(userId));
        accountService.createAccount(String.valueOf(userId), Currency.NGN);
        accountService.createAccount(String.valueOf(userId), Currency.USD);

        extraClaims.put("authorities", List.of("ROLE_USER"));
        extraClaims.put("userId", userId);

        jwt = jwtUtil.generateToken(extraClaims, "subject");
        headers.set("Authorization", "Bearer %s".formatted(jwt));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // when
        ResponseEntity<GetUserAccountsResponse> response = testRestTemplate.exchange(
                "/api/v1/wallets/accounts",
                HttpMethod.GET,
                requestEntity,
                GetUserAccountsResponse.class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        GetUserAccountsResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.accounts()).hasSize(2);
        assertThat(body.accounts())
                .allSatisfy(account -> assertThat(account.getUserId()).isEqualTo(userId));
    }

}