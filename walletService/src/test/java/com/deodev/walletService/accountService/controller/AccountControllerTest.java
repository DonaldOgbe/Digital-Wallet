package com.deodev.walletService.accountService.controller;

import com.deodev.walletService.accountService.dto.request.ReserveFundsRequest;
import com.deodev.walletService.accountService.dto.request.TransferFundsRequest;
import com.deodev.walletService.accountService.model.Account;
import com.deodev.walletService.accountService.model.FundReservation;
import com.deodev.walletService.accountService.repository.AccountRepository;
import com.deodev.walletService.accountService.repository.FundReservationRepository;
import com.deodev.walletService.client.UserServiceClient;
import com.deodev.walletService.dto.ApiResponse;
import com.deodev.walletService.dto.response.GetUserDetailsResponse;
import com.deodev.walletService.enums.Currency;
import com.deodev.walletService.enums.FundReservationStatus;
import com.deodev.walletService.walletPinService.model.WalletPin;
import com.deodev.walletService.walletPinService.repository.WalletPinRepository;
import com.deodev.walletService.walletService.model.Wallet;
import com.deodev.walletService.walletService.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private FundReservationRepository fundReservationRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private WalletPinRepository walletPinRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserServiceClient userServiceClient;


    private UUID userId;
    private UUID walletId;
    private String accountNumber;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        walletRepository.deleteAll();
        fundReservationRepository.deleteAll();
        userId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        accountNumber = "0123456789";
    }

    @Test
    void createAccount_ShouldReturn201_WhenRequestIsValid() throws Exception {
        // given
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .build();
        walletRepository.save(wallet);

        // when & then
        mockMvc.perform(post("/api/v1/wallets/accounts/{currency}", "NGN")
                        .header("X-User-Id", userId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.walletId").value(wallet.getId().toString()))
                .andExpect(jsonPath("$.data.accountId").isNotEmpty())
                .andExpect(jsonPath("$.data.accountNumber").isNotEmpty())
                .andExpect(jsonPath("$.data.currency").value("NGN"));
    }

    @Test
    void getRecipientDetails_ShouldReturnUserDetails_WhenAccountExists() throws Exception {
        // given
        Account account = Account.builder()
                .walletId(walletId)
                .userId(userId)
                .accountNumber(accountNumber)
                .currency(Currency.USD)
                .build();
        accountRepository.save(account);

        GetUserDetailsResponse userDetails = GetUserDetailsResponse.builder()
                .firstName("John").lastName("Doe").email("johndoe@email.com").build();

        ApiResponse<GetUserDetailsResponse> apiResponse =
                ApiResponse.success(200, userDetails);

        when(userServiceClient.getUser(eq(userId)))
                .thenReturn(apiResponse);

        // when & then
        mockMvc.perform(get("/api/v1/wallets/accounts/recipient/{accountNumber}", accountNumber)
                        .param("currency", "USD")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"));
    }

    @Test
    void getUserAccounts_ShouldReturnAccounts_WhenAccountExists() throws Exception {
        // given
        Account account = Account.builder()
                .walletId(walletId)
                .userId(userId)
                .accountNumber(accountNumber)
                .currency(Currency.USD)
                .build();
        accountRepository.save(account);

        // when & then
        mockMvc.perform(get("/api/v1/wallets/accounts")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accounts[0].accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.data.accounts[0].currency").value("USD"))
                .andExpect(jsonPath("$.data.accounts[0].userId").value(userId.toString()));
    }

    @Test
    void validateAndReserveFunds_ShouldReturnReservationId_WhenSufficientFunds() throws Exception {
        // given
        walletPinRepository.save(WalletPin.builder()
                .walletId(UUID.randomUUID())
                .userId(userId)
                .pin(passwordEncoder.encode("1234"))
                .build());
        String pin = "1234";

        Account account = Account.builder()
                .walletId(walletId).userId(userId)
                .accountNumber(accountNumber).currency(Currency.USD)
                .balance(500L).build();
        accountRepository.save(account);

        UUID transactionId = UUID.randomUUID();
        ReserveFundsRequest request = ReserveFundsRequest.builder()
                .pin(pin).accountNumber(accountNumber).transactionId(transactionId).amount(500L).build();

        // when & then
        mockMvc.perform(post("/api/v1/wallets/accounts/funds/reserve")
                        .header("X-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fundReservationId").isNotEmpty());
    }

//    @Test
//    void transferFunds_ShouldDebitSenderAndCreditReceiver_WhenReservationIsValid() throws Exception {
//        // given
//        UUID receiverWalletId = UUID.randomUUID();
//        UUID receiverUserId = UUID.randomUUID();
//        String receiverAccountNumber = "1111111111";
//
//        Account sender = Account.builder()
//                .walletId(walletId).userId(userId)
//                .accountNumber(accountNumber).currency(Currency.EUR)
//                .balance(500L).build();
//        accountRepository.save(sender);
//
//        Account receiver = Account.builder()
//                .walletId(receiverWalletId).userId(receiverUserId)
//                .accountNumber(receiverAccountNumber).currency(Currency.EUR)
//                .balance(100L).build();
//        accountRepository.save(receiver);
//
//        UUID transactionId = UUID.randomUUID();
//        FundReservation reservation = FundReservation.builder()
//                .transactionId(transactionId).accountNumber(accountNumber)
//                .amount(200L).status(FundReservationStatus.ACTIVE).build();
//        fundReservationRepository.save(reservation);
//
//        TransferFundsRequest request = TransferFundsRequest.builder()
//                .transactionId(transactionId).accountNumber(receiverAccountNumber)
//                .build();
//
//        // when
//        mockMvc.perform(post("/api/v1/wallets/accounts/funds/transfer")
//                        .header("X-User-Id", userId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data.transactionId").value(transactionId.toString()))
//                .andExpect(jsonPath("$.data.fundReservationId").value(reservation.getId().toString()));
//
//        // then
//        entityManager.clear();
//        Account updatedSender = accountRepository.findById(sender.getId()).orElseThrow();
//        Account updatedReceiver = accountRepository.findById(receiver.getId()).orElseThrow();
//        FundReservation updatedReservation = fundReservationRepository.findById(reservation.getId()).orElseThrow();
//
//        assertThat(updatedSender.getBalance()).isEqualTo(300L);
//        assertThat(updatedReceiver.getBalance()).isEqualTo(300L);
//        assertThat(updatedReservation.getStatus()).isEqualTo(FundReservationStatus.USED);
//    }

    @Test
    void releaseFunds_ShouldMarkReservationReleased_WhenReservationIsActive() throws Exception {
        // given
        UUID transactionId = UUID.randomUUID();
        FundReservation reservation = FundReservation.builder()
                .transactionId(transactionId).accountNumber(accountNumber)
                .amount(200L).status(FundReservationStatus.ACTIVE).build();
        fundReservationRepository.save(reservation);

        // when
        mockMvc.perform(post("/api/v1/wallets/accounts/funds/{transactionId}/release", transactionId)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionId").value(transactionId.toString()))
                .andExpect(jsonPath("$.data.fundReservationId").value(reservation.getId().toString()));

        // then
        entityManager.clear();
        FundReservation updated = fundReservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(FundReservationStatus.RELEASED);
    }

}