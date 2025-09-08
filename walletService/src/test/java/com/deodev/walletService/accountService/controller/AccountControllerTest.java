package com.deodev.walletService.accountService.controller;

import com.deodev.walletService.accountService.dto.request.ReserveFundsRequest;
import com.deodev.walletService.accountService.dto.request.TransferFundsRequest;
import com.deodev.walletService.accountService.dto.response.*;
import com.deodev.walletService.accountService.model.Account;
import com.deodev.walletService.accountService.model.FundReservation;
import com.deodev.walletService.accountService.repository.AccountRepository;
import com.deodev.walletService.accountService.repository.FundReservationRepository;
import com.deodev.walletService.accountService.service.AccountService;
import com.deodev.walletService.client.UserServiceClient;
import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.response.GetUserDetailsResponse;
import com.deodev.walletService.enums.Currency;
import com.deodev.walletService.enums.FundReservationStatus;
import com.deodev.walletService.util.JwtUtil;
import com.deodev.walletService.walletService.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Type;
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

    @Autowired
    private FundReservationRepository fundReservationRepository;

    @Autowired
    private AccountRepository accountRepository;

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
        ResponseEntity<ApiResponse<CreateAccountResponse>> response = testRestTemplate.exchange(
                "/api/v1/wallets/accounts/{currency}",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<ApiResponse<CreateAccountResponse>>() {
                },
                Currency.NGN
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        CreateAccountResponse body = response.getBody().getData();
        assertThat(body).isNotNull();
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.statusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(body.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(body.userId()).isEqualTo(userId);
        assertThat(body.currency()).isEqualTo(Currency.NGN);
        assertThat(body.accountId()).isNotNull();
        assertThat(body.walletId()).isNotNull();
        assertThat(body.accountNumber()).isNotBlank();
    }

    @Test
    void getRecipientDetailsAndSend200Response() {
        // given
        UUID userId = UUID.randomUUID();

        walletService.createWallet(userId.toString());
        CreateAccountResponse createdAccount = accountService.createAccount(userId.toString(), Currency.NGN);
        String recipientAccountNumber = createdAccount.accountNumber();

        ApiResponse<GetUserDetailsResponse> mockedUser = ApiResponse.success(
                HttpStatus.OK.value(),
                GetUserDetailsResponse.builder()
                        .username("recipientUsername")
                        .firstName("John")
                        .lastName("Doe")
                        .build());

        when(userServiceClient.getUser(any(), any())).thenReturn(mockedUser);

        extraClaims.put("authorities", List.of("ROLE_USER"));
        extraClaims.put("userId", userId);
        jwt = jwtUtil.generateToken(extraClaims, "subject");

        headers.set("Authorization", "Bearer %s".formatted(jwt));
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        // when
        ResponseEntity<ApiResponse<GetRecipientAccountUserDetailsResponse>> response = testRestTemplate.exchange(
                "/api/v1/wallets/accounts/recipient?accountNumber={accountNumber}",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<ApiResponse<GetRecipientAccountUserDetailsResponse>>() {
                },
                recipientAccountNumber
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        GetRecipientAccountUserDetailsResponse data = response.getBody().getData();
        assertThat(data).isNotNull();
        assertThat(data.isSuccess()).isTrue();
        assertThat(data.statusCode()).isEqualTo(HttpStatus.OK);
        assertThat(data.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(data.username()).isEqualTo("recipientUsername");
        assertThat(data.firstName()).isEqualTo("John");
        assertThat(data.lastName()).isEqualTo("Doe");
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
        ResponseEntity<ApiResponse<GetUserAccountsResponse>> response = testRestTemplate.exchange(
                "/api/v1/wallets/accounts",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<ApiResponse<GetUserAccountsResponse>>() {
                }
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ApiResponse<GetUserAccountsResponse> body = response.getBody();

        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(body.getData()).isNotNull();
    }

    @Test
    void reserveFunds_CreatesFundReservation_Sends200Response() {
        // given
        UUID userId = UUID.randomUUID();
        walletService.createWallet(String.valueOf(userId));
        CreateAccountResponse createAccountResponse = accountService.createAccount(String.valueOf(userId), Currency.NGN);
        accountService.creditBalance(createAccountResponse.accountNumber(), 100L);

        ReserveFundsRequest request = ReserveFundsRequest.builder()
                .accountNumber(createAccountResponse.accountNumber())
                .amount(100L)
                .transactionId(UUID.randomUUID())
                .build();

        extraClaims.put("authorities", List.of("ROLE_USER"));
        extraClaims.put("userId", userId);
        String jwt = jwtUtil.generateToken(extraClaims, "subject");

        headers.add("Authorization", "Bearer %s".formatted(jwt));

        HttpEntity<ReserveFundsRequest> requestEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<ApiResponse<ReserveFundsResponse>> response = testRestTemplate.exchange(
                "/api/v1/wallets/accounts/funds/reserve",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<ApiResponse<ReserveFundsResponse>>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ApiResponse<ReserveFundsResponse> body = response.getBody();

        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(body.getData()).isNotNull();
    }

    @Test
    void transferFunds_Sends200Response() {
        // given
        UUID senderUserId = UUID.randomUUID();
        UUID receiverUserId = UUID.randomUUID();

        walletService.createWallet(String.valueOf(senderUserId));
        walletService.createWallet(String.valueOf(receiverUserId));

        CreateAccountResponse senderAccount = accountService.createAccount(String.valueOf(senderUserId), Currency.NGN);
        CreateAccountResponse receiverAccount = accountService.createAccount(String.valueOf(receiverUserId), Currency.NGN);

        accountService.creditBalance(senderAccount.accountNumber(), 1000L);

        UUID transactionId = UUID.randomUUID();
        FundReservation reservation = FundReservation.builder()
                .accountNumber(senderAccount.accountNumber())
                .transactionId(transactionId)
                .amount(200L)
                .status(FundReservationStatus.ACTIVE)
                .expiredAt(LocalDateTime.now().plusHours(1))
                .build();
        fundReservationRepository.save(reservation);

        TransferFundsRequest request = TransferFundsRequest.builder()
                .accountNumber(receiverAccount.accountNumber())
                .currency(Currency.NGN)
                .amount(200L)
                .transactionId(transactionId)
                .build();

        extraClaims.put("authorities", List.of("ROLE_USER"));
        String jwt = jwtUtil.generateToken(extraClaims, "subject");
        headers.add("Authorization", "Bearer %s".formatted(jwt));

        HttpEntity<TransferFundsRequest> requestEntity = new HttpEntity<>(request, headers);

        // when
        ResponseEntity<ApiResponse<TransferFundsResponse>> response = testRestTemplate.exchange(
                "/api/v1/wallets/accounts/funds/transfer",
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<ApiResponse<TransferFundsResponse>>() {}
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ApiResponse<TransferFundsResponse> body = response.getBody();
        assertThat(body.isSuccess()).isTrue();
        assertThat(body.getStatusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(body.getData()).isNotNull();

        // Verify balances
        Account updatedSender = accountRepository.findByAccountNumber(senderAccount.accountNumber()).get();
        Account updatedReceiver = accountRepository.findByAccountNumber(receiverAccount.accountNumber()).get();

        assertThat(updatedSender.getBalance()).isEqualTo(800L);
        assertThat(updatedReceiver.getBalance()).isEqualTo(200L);

        // Verify reservation updated
        FundReservation updatedReservation = fundReservationRepository.findByTransactionId(transactionId).get();
        assertThat(updatedReservation.getStatus()).isEqualTo(FundReservationStatus.USED);
        assertThat(updatedReservation.getUsedAt()).isNotNull();
    }
}