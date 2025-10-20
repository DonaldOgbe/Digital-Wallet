package com.deodev.transactionService.pspService.flutterwave.service;

import com.deodev.transactionService.pspService.flutterwave.client.FlutterwaveClient;
import com.deodev.transactionService.rabbitmq.outbox.service.OutboxService;
import com.deodev.transactionService.transactionService.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlutterwaveCardServiceTest {

    @Mock
    private FlutterwaveClient flutterwaveClient;

    @Mock
    private TransactionService transactionService;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private FlutterwaveCardService flutterwaveCardService;

    @Test
    void chargeCardResponse_shouldFilterCorrectly_whenAuthorizationPresent() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("id", 123);
        data.put("txn_ref", "TX123");
        data.put("flw_ref", "FLW123");
        data.put("processor_response", "Approved");
        data.put("status", "pending");

        Map<String, Object> auth = new HashMap<>();
        auth.put("mode", "otp");
        auth.put("other_field", "value");

        Map<String, Object> meta = new HashMap<>();
        meta.put("authorization", auth);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);
        response.put("meta", meta);

        // when
        Map<String, Object> filtered = flutterwaveCardService.filterChargeCardResponse(response);

        // then
        assertThat(filtered.get("id")).isEqualTo(123);
        assertThat(filtered.get("txn_ref")).isEqualTo("TX123");
        assertThat(filtered.get("flw_ref")).isEqualTo("FLW123");
        assertThat(filtered.get("processor_response")).isEqualTo("Approved");
        assertThat(filtered.get("status")).isEqualTo("pending");
        assertThat(filtered.get("mode")).isEqualTo("otp");
        assertThat(filtered.get("authorization")).isEqualTo(auth);
    }

    @Test
    void chargeCardResponse_shouldReturnModeNone_whenMetaMissing() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("id", 456);
        data.put("status", "success");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", data);


        // when
        Map<String, Object> filtered = flutterwaveCardService.filterChargeCardResponse(response);

        // then
        assertThat(filtered.get("id")).isEqualTo(456);
        assertThat(filtered.get("status")).isEqualTo("success");
        assertThat(filtered.get("mode")).isEqualTo("none");
        assertThat(filtered.get("authorization")).isNotNull();
        assertThat(((Map<?, ?>) filtered.get("authorization"))).isEmpty();
    }

    @Test
    void chargeCard_ShouldReturnFilteredResponse_WhenSuccess() throws Exception {
        // given
        String encryptedPayload = "ENCRYPTED_STRING";

        Map<String, Object> flutterwaveResponse = Map.of(
                "status", "success",
                "message", "Charge initiated",
                "data", Map.of(
                        "id", 1254647,
                        "txn_ref", "UNIQUE_TRANSACTION_REFERENCE",
                        "flw_ref", "IUSE9942171639769110812191",
                        "processor_response", "Pending redirect to issuer's 3DS authentication page",
                        "status", "pending"
                ),
                "meta", Map.of(
                        "authorization", Map.of(
                                "mode", "redirect",
                                "redirect", "https://auth.coreflutterwaveprod.com/transaction?reference=IUSE9942171639769110812191"
                        )
                )
        );

        when(flutterwaveClient.chargeCard(any())).thenReturn(flutterwaveResponse);

        // when
        Map<String, Object> response = flutterwaveCardService.chargeCard(encryptedPayload);

        // then
        assertThat(response).isNotNull();
        assertThat(response.get("id")).isEqualTo(1254647);
        assertThat(response.get("txn_ref")).isEqualTo("UNIQUE_TRANSACTION_REFERENCE");
        assertThat(response.get("flw_ref")).isEqualTo("IUSE9942171639769110812191");
        assertThat(response.get("processor_response")).isEqualTo("Pending redirect to issuer's 3DS authentication page");
        assertThat(response.get("status")).isEqualTo("pending");
        assertThat(response.get("mode")).isEqualTo("redirect");
        assertThat(response.get("authorization")).isEqualTo(Map.of(
                "mode", "redirect",
                "redirect", "https://auth.coreflutterwaveprod.com/transaction?reference=IUSE9942171639769110812191"
        ));

        verify(flutterwaveClient).chargeCard(any());
    }

}