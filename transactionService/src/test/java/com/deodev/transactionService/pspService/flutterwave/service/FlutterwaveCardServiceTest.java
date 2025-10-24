package com.deodev.transactionService.pspService.flutterwave.service;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.enums.Currency;
import com.deodev.transactionService.enums.ErrorCode;
import com.deodev.transactionService.pspService.flutterwave.client.FlutterwaveClient;
import com.deodev.transactionService.pspService.flutterwave.dto.FlutterwaveResponse;
import com.deodev.transactionService.pspService.flutterwave.dto.response.FilteredChargeCardResponse;
import com.deodev.transactionService.pspService.flutterwave.dto.response.FilteredVerifyChargeCardResponse;
import com.deodev.transactionService.pspService.flutterwave.dto.response.VerifyChargeCardResponse;
import com.deodev.transactionService.pspService.flutterwave.util.FlutterwaveCardServiceHelper;
import com.deodev.transactionService.rabbitmq.outbox.service.OutboxService;
import com.deodev.transactionService.transactionService.model.CardFundingTransaction;
import com.deodev.transactionService.transactionService.model.Transaction;
import com.deodev.transactionService.transactionService.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlutterwaveCardServiceTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private FlutterwaveCardService flutterwaveCardService;

    private Transaction transaction;
    private CardFundingTransaction cardFundingTransaction;

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAmount(7500L);
        transaction.setCurrency(Currency.NGN);

        cardFundingTransaction = new CardFundingTransaction();
        cardFundingTransaction.setId(UUID.randomUUID());
    }


    @Nested
    class verifyResponse {
        @Test
        void verifyResponse_ShouldReturnSuccess_WhenStatusSuccessful() throws Exception {
            // given
            FilteredVerifyChargeCardResponse response = FilteredVerifyChargeCardResponse.builder()
                    .data_status("successful")
                    .message("Charge successful")
                    .id(123)
                    .flw_ref("FLW12345")
                    .build();

            // when
            ApiResponse<?> result = flutterwaveCardService.verifyResponse(response, cardFundingTransaction, transaction);

            // then
            assertThat(result.isSuccess()).isTrue();
            VerifyChargeCardResponse data = (VerifyChargeCardResponse) result.getData();
            assertThat(data.status()).isEqualTo("successful");
            assertThat(data.txn_ref()).isEqualTo(cardFundingTransaction.getId().toString());
            assertThat(data.currency()).isEqualTo(Currency.NGN);

            verify(transactionService, never())
                    .setFailedCardFundingTransaction(any(), any(), any());
        }

        @Test
        void verifyResponse_ShouldReturnSuccess_WhenStatusPending() throws Exception {
            // given
            FilteredVerifyChargeCardResponse response = FilteredVerifyChargeCardResponse.builder()
                    .data_status("pending")
                    .message("Waiting for authorization")
                    .id(124)
                    .flw_ref("FLW6789")
                    .build();

            // when
            ApiResponse<?> result = flutterwaveCardService.verifyResponse(response, cardFundingTransaction, transaction);

            // then
            assertThat(result.isSuccess()).isTrue();
            VerifyChargeCardResponse data = (VerifyChargeCardResponse) result.getData();
            assertThat(data.status()).isEqualTo("pending");
            assertThat(data.txn_ref()).isEqualTo(cardFundingTransaction.getId().toString());

            verify(transactionService, never())
                    .setFailedCardFundingTransaction(any(), any(), any());
        }

        @Test
        void verifyResponse_ShouldMarkFailed_WhenStatusIsUnknown() throws Exception {
            // given
            FilteredVerifyChargeCardResponse response = FilteredVerifyChargeCardResponse.builder()
                    .data_status("failed")
                    .message("Transaction declined")
                    .id(124)
                    .flw_ref("FLW00001")
                    .build();

            // when
            ApiResponse<?> result = flutterwaveCardService.verifyResponse(response, cardFundingTransaction, transaction);

            // then
            assertThat(result.isSuccess()).isTrue();
            VerifyChargeCardResponse data = (VerifyChargeCardResponse) result.getData();
            assertThat(data.status()).isEqualTo("failed");

            verify(transactionService)
                    .setFailedCardFundingTransaction(eq(cardFundingTransaction), eq(transaction), eq(ErrorCode.EXTERNAL_PSP_ERROR));
        }
    }


}