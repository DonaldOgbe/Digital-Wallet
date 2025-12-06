package com.deodev.transactionService.pspService.flutterwave.util;

import com.deodev.transactionService.exception.ExternalServiceException;
import com.deodev.transactionService.exception.PSPException;
import com.deodev.transactionService.pspService.flutterwave.dto.FlutterwaveResponse;
import com.deodev.transactionService.pspService.flutterwave.dto.response.FilteredChargeCardResponse;
import com.deodev.transactionService.pspService.flutterwave.dto.response.FilteredValidateOtpResponse;
import com.deodev.transactionService.pspService.flutterwave.dto.response.FilteredVerifyChargeCardResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@NoArgsConstructor
public class FlutterwaveCardServiceHelper {

    public void handleErrorStatus(FlutterwaveResponse response) {
        if (!"success".equalsIgnoreCase(response.status())) {
            log.warn("{} Response: {}", response.message(), response);
            throw new ExternalServiceException(response.message());
        }
    }

    public FilteredChargeCardResponse filterChargeCardResponse(FlutterwaveResponse response) {
        Map<String, Object> data = response.data();
        Map<String, Object> meta = response.meta();
        Map<String, Object> auth = meta != null && meta.get("authorization") instanceof Map
                ? (Map<String, Object>) meta.get("authorization")
                : Map.of();

        return new FilteredChargeCardResponse(
                data != null ? Long.valueOf((Integer) data.get("id")) : null,
                data != null ? (String) data.get("txn_ref") : null,
                data != null ? (String) data.get("flw_ref") : null,
                data != null ? (String) data.get("processor_response") : null,
                data != null ? (String) data.get("status") : response.status(),
                auth.get("mode") != null ? (String) auth.get("mode") : "none",
                auth
        );
    }

    public FilteredValidateOtpResponse filterValidateOtpResponse(FlutterwaveResponse response) {
        Map<String, Object> data = response.data();

        return FilteredValidateOtpResponse.builder()
                .status(response.status())
                .message(response.message())
                .id(data != null ? (Integer) data.get("id") : null)
                .build();
    }

    public FilteredVerifyChargeCardResponse filterVerifyChargeCard(FlutterwaveResponse response) {
        Map<String, Object> data = response.data();

        return FilteredVerifyChargeCardResponse.builder()
                .status(response.status())
                .message(response.message())
                .id(data != null ? (Integer) data.get("id") : null)
                .flw_ref(data != null ? (String) data.get("flw_ref") : null)
                .txn_ref(data != null ? (String) data.get("txn_ref") : null)
                .processor_response(data != null ? (String) data.get("processor_response") : null)
                .data_status(data != null ? (String) data.get("status") : null).build();
    }
}
