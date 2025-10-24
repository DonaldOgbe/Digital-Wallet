package com.deodev.transactionService.pspService.flutterwave.service;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.ErrorResponse;
import com.deodev.transactionService.enums.*;
import com.deodev.transactionService.exception.ExternalServiceException;
import com.deodev.transactionService.exception.PSPException;
import com.deodev.transactionService.pspService.flutterwave.client.FlutterwaveClient;
import com.deodev.transactionService.pspService.flutterwave.dto.request.*;
import com.deodev.transactionService.pspService.flutterwave.dto.response.CardTypePayload;
import com.deodev.transactionService.pspService.flutterwave.dto.response.ChargeCardResponse;
import com.deodev.transactionService.pspService.flutterwave.dto.response.ValidateChargeCardResponse;
import com.deodev.transactionService.pspService.flutterwave.dto.response.VerifyChargeCardResponse;
import com.deodev.transactionService.pspService.walletService.service.WalletService;
import com.deodev.transactionService.rabbitmq.events.AccountFundedEvent;
import com.deodev.transactionService.rabbitmq.outbox.service.OutboxService;
import com.deodev.transactionService.transactionService.model.CardFundingTransaction;
import com.deodev.transactionService.transactionService.model.Transaction;
import com.deodev.transactionService.transactionService.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.deodev.transactionService.rabbitmq.constants.keys.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlutterwaveCardService {

    private final FlutterwaveClient flutterwaveClient;
    private final ObjectMapper mapper;
    private final TransactionService transactionService;
    private final WalletService walletService;
    private final OutboxService outboxService;

    public ApiResponse<?> getCardType(String bin) {
        Map<String, Object> response = flutterwaveClient.resolveCard(bin);
        handleErrorStatus(response, "Flutterwave failed to resolve card type for BIN " + bin);

        Map<String, Object> data = (Map<String, Object>) response.get("data");
        String cardType = (data != null) ? (String) data.getOrDefault("card_type", "UNKNOWN") : "UNKNOWN";

        return ApiResponse.success(HttpStatus.OK.value(), new CardTypePayload(cardType));
    }

    public ApiResponse<?> initiateChargeCard(InitiateChargeCardRequest request,
                                             String userId, String idempotencyKey) {
        if (!walletService.verifyAccountNumber(request.accountNumber(), request.currency())) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ErrorCode.NOT_FOUND,
                    ErrorResponse.builder().message("Account number %s does not exists".formatted(request.accountNumber())));
        }

        Transaction transaction = createNewTransaction(request, userId, TransactionType.CARD_FUND, idempotencyKey);
        CardFundingTransaction cardFundingTransaction = createNewCardFundingTransaction(request, transaction.getId());

        try {
            return processChargeCardTransaction(request.client(), cardFundingTransaction, transaction);
        } catch (ExternalServiceException ex) {
            transactionService.setFailedCardFundingTransaction(cardFundingTransaction, transaction, ErrorCode.EXTERNAL_PSP_ERROR);
            log.error("External PSP error [txnId={}, idKey={}]: {}", transaction.getId(), idempotencyKey, ex.getMessage(), ex);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.EXTERNAL_PSP_ERROR,
                    ErrorResponse.builder().message("PSP service temporarily unavailable").build());
        } catch (Exception ex) {
            transactionService.setFailedCardFundingTransaction(cardFundingTransaction, transaction, ErrorCode.EXTERNAL_PSP_ERROR);
            log.error("Transaction {} failed on charge card due to exception", transaction.getId(), ex);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.SYSTEM_ERROR,
                    ErrorResponse.builder().message("Internal processing error"));
        }
    }

    public ApiResponse<?> completeChargeCard(CompleteChargeCardRequest request) {
        CardFundingTransaction cardFundingTransaction = transactionService.getCardFundingTransaction(
                UUID.fromString(request.txn_ref()));

        Transaction transaction = transactionService.getTransaction(cardFundingTransaction.getTransactionId());

        try {
            return processChargeCardTransaction(request.client(), cardFundingTransaction, transaction);
        } catch (ExternalServiceException ex) {
            transactionService.setFailedCardFundingTransaction(cardFundingTransaction, transaction, ErrorCode.EXTERNAL_PSP_ERROR);
            log.error("External PSP error txnId={}: {}", transaction.getId(), ex.getMessage(), ex);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.EXTERNAL_PSP_ERROR,
                    ErrorResponse.builder().message("PSP service temporarily unavailable").build());
        } catch (Exception ex) {
            transactionService.setFailedCardFundingTransaction(cardFundingTransaction, transaction, ErrorCode.EXTERNAL_PSP_ERROR);
            log.error("Transaction {} failed on complete charge card due to exception", transaction.getId(), ex);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.SYSTEM_ERROR,
                    ErrorResponse.builder().message("Internal processing error"));
        }
    }

    public ApiResponse<?> validateChargeCard(ValidateChargeCardRequest request) {
        CardFundingTransaction cardFundingTransaction = transactionService.getCardFundingTransaction(
                UUID.fromString(request.txn_ref()));

        Transaction transaction = transactionService.getTransaction(cardFundingTransaction.getTransactionId());

        OtpValidateRequest otpValidateRequest = OtpValidateRequest.builder()
                .otp(request.otp()).flw_ref(request.flw_ref()).build();

        Map<String, Object> response = new HashMap<>();
        try {
            response = validateOtp(otpValidateRequest);
        } catch (ExternalServiceException ex) {
            transactionService.setFailedCardFundingTransaction(cardFundingTransaction, transaction, ErrorCode.EXTERNAL_PSP_ERROR);
            log.error("External PSP error txnId={}: {}", transaction.getId(), ex.getMessage(), ex);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.EXTERNAL_PSP_ERROR,
                    ErrorResponse.builder().message("PSP service temporarily unavailable").build());
        } catch (Exception ex) {
            transactionService.setFailedCardFundingTransaction(cardFundingTransaction, transaction, ErrorCode.SYSTEM_ERROR);
            log.error("Transaction {} failed while validating charge card due to exception", transaction.getId(), ex);
            throw ex;
        }

        return ApiResponse.success(HttpStatus.OK.value(), ValidateChargeCardResponse.builder()
                .status((String) response.get("status"))
                .message((String) response.get("message"))
                .id(Long.valueOf((Integer) response.get("id")))
                .build());
    }

    public ApiResponse<?> verifyCardTransaction(VerifyChargeCardRequest request) throws Exception {
        CardFundingTransaction cardFundingTransaction = transactionService.getCardFundingTransaction(
                UUID.fromString(request.txn_ref()));

        Transaction transaction = transactionService.getTransaction(cardFundingTransaction.getTransactionId());

        Map<String, Object> response = new HashMap<>();
        try {
            response = verifyChargeCard(request.id());
        } catch (ExternalServiceException ex) {
            log.error("External PSP error txnId={}: {}", transaction.getId(), ex.getMessage(), ex);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ErrorCode.EXTERNAL_PSP_ERROR,
                    ErrorResponse.builder().message("PSP service temporarily unavailable").build());
        } catch (Exception ex) {
            log.error("Transaction {} failed while verifying charge card due to exception", transaction.getId(), ex);
            throw ex;
        }

        return verifyResponse(response, cardFundingTransaction, transaction);
    }

    ApiResponse<?> verifyResponse(Map<String, Object> response,
                                  CardFundingTransaction cardFundingTransaction, Transaction transaction) throws Exception {
        return switch ((String) response.get("data_status")) {
            case "successful" -> {
                setSuccessfulCardFunding(cardFundingTransaction, transaction);
                yield ApiResponse.success(HttpStatus.OK.value(), VerifyChargeCardResponse.builder()
                        .status((String) response.get("data_status"))
                        .message((String) response.get("processor_response"))
                        .id(Long.valueOf((Integer) response.get("id")))
                        .txn_ref(cardFundingTransaction.getId().toString())
                        .flw_ref((String) response.get("flw_ref"))
                        .transactionId(transaction.getId().toString())
                        .amount(transaction.getAmount())
                        .currency(transaction.getCurrency())
                        .build());
            }
            case "pending" -> ApiResponse.success(HttpStatus.OK.value(), VerifyChargeCardResponse.builder()
                    .status((String) response.get("data_status"))
                    .message((String) response.get("processor_response"))
                    .id(Long.valueOf((Integer) response.get("id")))
                    .txn_ref(cardFundingTransaction.getId().toString())
                    .flw_ref((String) response.get("flw_ref"))
                    .transactionId(transaction.getId().toString())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .build());
            default -> {
                transactionService.setFailedCardFundingTransaction(cardFundingTransaction, transaction, ErrorCode.EXTERNAL_PSP_ERROR);
                yield ApiResponse.success(HttpStatus.OK.value(), VerifyChargeCardResponse.builder()
                        .status((String) response.get("data_status"))
                        .message((String) response.get("processor_response"))
                        .id(Long.valueOf((Integer) response.get("id")))
                        .txn_ref(cardFundingTransaction.getId().toString())
                        .flw_ref((String) response.get("flw_ref"))
                        .transactionId(transaction.getId().toString())
                        .amount(transaction.getAmount())
                        .currency(transaction.getCurrency())
                        .build());
            }
        };
    }

    @Transactional
    void setSuccessfulCardFunding(CardFundingTransaction cardFundingTransaction, Transaction transaction) throws Exception {
        transactionService.setSuccessfulCardFundingTransaction(cardFundingTransaction, transaction);

        String eventId = UUID.randomUUID().toString();

        outboxService.createScheduledEvent(
                eventId,
                ACCOUNT_FUNDED,
                EventType.ACCOUNT_FUNDED,
                AccountFundedEvent.builder()
                        .eventId(eventId)
                        .accountNumber(transaction.getAccountNumber())
                        .amount(transaction.getAmount())
                        .build());
    }

    Map<String, Object> chargeCard(String client) {
        EncryptedChargeRequest chargeRequest = new EncryptedChargeRequest(client);

        Map<String, Object> response = flutterwaveClient.chargeCard(chargeRequest);
        handleErrorStatus(response, "Flutterwave failed to charge card");

        return filterChargeCardResponse(response);
    }

    Map<String, Object> validateOtp(OtpValidateRequest request) {
        Map<String, Object> response = flutterwaveClient.validateCharge(request);
        handleErrorStatus(response, "Flutterwave failed to validate charge with otp");

        return filterValidateOtpResponse(response);
    }

    Map<String, Object> verifyChargeCard(Long id) {
        Map<String, Object> response = flutterwaveClient.verifyCharge(id);
        handleErrorStatus(response, "Flutterwave failed to verify charge");

        return filterVerifyChargeCard(response);
    }

    ApiResponse<?> processChargeCardTransaction(String client, CardFundingTransaction cardFundingTransaction, Transaction transaction) {
        Map<String, Object> response = new HashMap<>();

        response = chargeCard(client);

        CardFundingTransaction savedCardFundingTransaction = transactionService.getCardFundingTransaction(cardFundingTransaction.getId());

        Object id = response.get("id");
        Long longId = (id instanceof Number) ? ((Number) id).longValue() : null;

        savedCardFundingTransaction.setGatewayTransactionId(longId);
        savedCardFundingTransaction.setGatewayReference((String) response.get("flw_ref"));
        savedCardFundingTransaction.setAuthorizationCode((String) response.get("mode"));
        transactionService.saveCardFundingTransaction(savedCardFundingTransaction);
        Map<String, Object> auth = (Map<String, Object>) response.get("authorization");

        return ApiResponse.success(HttpStatus.OK.value(), ChargeCardResponse.builder()
                .txn_ref(cardFundingTransaction.getId().toString())
                .id(longId)
                .mode((String) response.get("mode"))
                .flw_ref((String) response.get("flw_ref"))
                .redirect(auth != null ? (String) auth.get("redirect") : null)
                .message((String) response.get("processor_response"))
                .build());
    }

    void handleErrorStatus(Map<String, Object> response, String message) {
        if (!"success".equalsIgnoreCase((String) response.get("status"))) {
            log.warn("{}Response: {}", message, response);
            throw new PSPException(response.get("message") != null ? (String) response.get("message") : message);
        }
    }

    Map<String, Object> filterChargeCardResponse(Map<String, Object> response) {
        Map<String, Object> filtered = new HashMap<>();

        Map<String, Object> data = (Map<String, Object>) response.getOrDefault("data", Map.of());
        Map<String, Object> meta = (Map<String, Object>) response.getOrDefault("meta", Map.of());
        Map<String, Object> auth = meta != null && meta.get("authorization") instanceof Map
                ? (Map<String, Object>) meta.get("authorization")
                : Map.of();

        filtered.put("id", data != null ? data.get("id") : null);
        filtered.put("txn_ref", data != null ? data.get("txn_ref") : null);
        filtered.put("flw_ref", data != null ? data.get("flw_ref") : null);
        filtered.put("processor_response", data != null ? data.get("processor_response") : null);
        filtered.put("status", data != null ? data.get("status") : response.get("status"));
        filtered.put("mode", auth.get("mode") != null ? auth.get("mode") : "none");
        filtered.put("authorization", auth);

        return filtered;
    }

    Map<String, Object> filterValidateOtpResponse(Map<String, Object> response) {
        Map<String, Object> filtered = new HashMap<>();

        String status = (String) response.get("status");
        String message = (String) response.get("message");
        Map<String, Object> data = (Map<String, Object>) response.getOrDefault("data", Map.of());

        filtered.put("status", status);
        filtered.put("message", message);
        filtered.put("id", data != null ? data.get("id") : null);

        return filtered;
    }

    Map<String, Object> filterVerifyChargeCard(Map<String, Object> response) {
        Map<String, Object> filtered = new HashMap<>();

        String status = (String) response.get("status");
        String message = (String) response.get("message");
        Map<String, Object> data = (Map<String, Object>) response.getOrDefault("data", Map.of());

        filtered.put("status", status);
        filtered.put("message", message);
        filtered.put("id", data != null ? data.get("id") : null);
        filtered.put("txn_ref", data != null ? data.get("txn_ref") : null);
        filtered.put("flw_ref", data != null ? data.get("flw_ref") : null);
        filtered.put("processor_response", data != null ? data.get("processor_response") : null);
        filtered.put("data_status", data != null ? data.get("status") : null);

        return filtered;
    }

    @Transactional
    Transaction createNewTransaction(InitiateChargeCardRequest request,
                                     String userId, TransactionType type,
                                     String idempotencyKey) {
        Transaction transaction = Transaction.builder()
                .transactionType(type)
                .userId(UUID.fromString(userId))
                .accountNumber(request.accountNumber())
                .amount(request.amount())
                .currency(request.currency())
                .status(TransactionStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .build();

        return transactionService.saveTransaction(transaction);
    }

    @Transactional
    CardFundingTransaction createNewCardFundingTransaction(InitiateChargeCardRequest request, UUID transactionId) {
        CardFundingTransaction cardFundingTransaction = CardFundingTransaction.builder()
                .id(UUID.fromString(request.txn_ref()))
                .transactionId(transactionId)
                .accountNumber(request.accountNumber())
                .cardLast4(request.cardLast4())
                .cardType(request.cardType())
                .paymentGateway(PaymentGateway.FLUTTERWAVE)
                .status(TransactionStatus.PENDING)
                .build();

        return transactionService.saveCardFundingTransaction(cardFundingTransaction);
    }
}
