package com.deodev.transactionService.pspService.flutterwave.util;

import com.deodev.transactionService.pspService.flutterwave.dto.FlutterwaveResponse;
import com.deodev.transactionService.pspService.flutterwave.dto.response.FilteredChargeCardResponse;
import com.deodev.transactionService.pspService.flutterwave.dto.response.FilteredValidateOtpResponse;
import com.deodev.transactionService.pspService.flutterwave.dto.response.FilteredVerifyChargeCardResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class FlutterwaveCardServiceHelperTest {

    private FlutterwaveCardServiceHelper flutterwaveCardServiceHelper;

    @BeforeEach
    void setup() {
        flutterwaveCardServiceHelper = new FlutterwaveCardServiceHelper();
    }

    @Test
    void filterChargeCardResponse_shouldFilterCorrectly_whenAuthorizationPresent() {
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

        FlutterwaveResponse response = new FlutterwaveResponse(
                "success",
                "Charge initiated",
                data,
                meta
        );

        // when
        FilteredChargeCardResponse filtered = flutterwaveCardServiceHelper.filterChargeCardResponse(response);

        // then
        assertThat(filtered.id()).isEqualTo(123);
        assertThat(filtered.txn_ref()).isEqualTo("TX123");
        assertThat(filtered.flw_ref()).isEqualTo("FLW123");
        assertThat(filtered.processor_response()).isEqualTo("Approved");
        assertThat(filtered.status()).isEqualTo("pending");
        assertThat(filtered.mode()).isEqualTo("otp");
        assertThat(filtered.authorization()).isEqualTo(auth);
    }

    @Test
    void filterChargeCardResponse_shouldReturnModeNone_whenMetaMissing() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("id", 456);
        data.put("status", "success");

        FlutterwaveResponse response = new FlutterwaveResponse(
                "success",
                "Charge initiated",
                data,
                null
        );

        // when
        FilteredChargeCardResponse filtered = flutterwaveCardServiceHelper.filterChargeCardResponse(response);

        // then
        assertThat(filtered.id()).isEqualTo(456);
        assertThat(filtered.status()).isEqualTo("success");
        assertThat(filtered.mode()).isEqualTo("none");
        assertThat(filtered.authorization()).isNotNull();
        assertThat(filtered.authorization()).isEmpty();
    }

    @Test
    void filterValidateOtpResponse_shouldFilterCorrectly() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("id", 789);

        FlutterwaveResponse response = new FlutterwaveResponse(
                "success",
                "OTP validated successfully",
                data,
                null
        );

        // when
        FilteredValidateOtpResponse filtered = flutterwaveCardServiceHelper.filterValidateOtpResponse(response);

        // then
        assertThat(filtered.status()).isEqualTo("success");
        assertThat(filtered.message()).isEqualTo("OTP validated successfully");
        assertThat(filtered.id()).isEqualTo(789);
    }

    @Test
    void filterVerifyChargeCard_shouldFilterCorrectly() {
        // given
        Map<String, Object> data = new HashMap<>();
        data.put("id", 111);
        data.put("flw_ref", "FLW-XYZ");
        data.put("txn_ref", "TX-999");
        data.put("processor_response", "Approved");
        data.put("status", "successful");

        FlutterwaveResponse response = new FlutterwaveResponse(
                "success",
                "Transaction fetched successfully",
                data,
                null
        );

        // when
        FilteredVerifyChargeCardResponse filtered = flutterwaveCardServiceHelper.filterVerifyChargeCard(response);

        // then
        assertThat(filtered.status()).isEqualTo("success");
        assertThat(filtered.message()).isEqualTo("Transaction fetched successfully");
        assertThat(filtered.id()).isEqualTo(111);
        assertThat(filtered.flw_ref()).isEqualTo("FLW-XYZ");
        assertThat(filtered.txn_ref()).isEqualTo("TX-999");
        assertThat(filtered.processor_response()).isEqualTo("Approved");
        assertThat(filtered.data_status()).isEqualTo("successful");
    }

}