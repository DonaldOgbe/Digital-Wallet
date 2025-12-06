package com.deodev.transactionService.pspService.flutterwave.service;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.ErrorResponse;
import com.deodev.transactionService.enums.*;
import com.deodev.transactionService.exception.ExternalServiceException;
import com.deodev.transactionService.pspService.flutterwave.client.FlutterwaveClient;
import com.deodev.transactionService.pspService.flutterwave.dto.FlutterwaveResponse;
import com.deodev.transactionService.pspService.flutterwave.dto.request.*;
import com.deodev.transactionService.pspService.flutterwave.dto.response.*;
import com.deodev.transactionService.pspService.flutterwave.util.FlutterwaveCardServiceHelper;
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
    private final FlutterwaveCardServiceHelper helper;

    // Resolve Card BIN

    public ApiResponse<?> getCardType(String bin) {
        FlutterwaveResponse response = flutterwaveClient.resolveCard(bin);
        helper.handleErrorStatus(response);

        Map<String, Object> data = response.data();
        String cardType = (data != null) ? (String) data.getOrDefault("card_type", "UNKNOWN") : "UNKNOWN";

        return ApiResponse.success(HttpStatus.OK.value(), new CardTypePayload(cardType));
    }

    // Charge Card

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
                    ErrorResponse.builder().message("Internal processing error").build());
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

    ApiResponse<?> processChargeCardTransaction(String client, CardFundingTransaction cardFundingTransaction, Transaction transaction) {
        FilteredChargeCardResponse response = chargeCard(client);

        CardFundingTransaction savedCardFundingTransaction = transactionService.getCardFundingTransaction(cardFundingTransaction.getId());

        savedCardFundingTransaction.setGatewayTransactionId(response.id());
        savedCardFundingTransaction.setGatewayReference(response.flw_ref());
        savedCardFundingTransaction.setAuthorizationCode(response.mode());
        transactionService.saveCardFundingTransaction(savedCardFundingTransaction);
        Map<String, Object> auth = response.authorization();

        return ApiResponse.success(HttpStatus.OK.value(), ChargeCardResponse.builder()
                .txn_ref(cardFundingTransaction.getId().toString())
                .id(response.id())
                .mode(response.mode())
                .flw_ref(response.flw_ref())
                .redirect(auth != null ? (String) auth.get("redirect") : null)
                .message(response.processor_response())
                .build());
    }

    FilteredChargeCardResponse chargeCard(String client) {
        EncryptedChargeRequest chargeRequest = new EncryptedChargeRequest(client);

        FlutterwaveResponse response = flutterwaveClient.chargeCard(chargeRequest);
        helper.handleErrorStatus(response);

        return helper.filterChargeCardResponse(response);
    }

    // Validate OTP

    public ApiResponse<?> validateChargeCard(ValidateChargeCardRequest request) {
        CardFundingTransaction cardFundingTransaction = transactionService.getCardFundingTransaction(
                UUID.fromString(request.txn_ref()));

        Transaction transaction = transactionService.getTransaction(cardFundingTransaction.getTransactionId());

        OtpValidateRequest otpValidateRequest = OtpValidateRequest.builder()
                .otp(request.otp()).flw_ref(request.flw_ref()).build();

        FilteredValidateOtpResponse response;
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
                .status(response.status())
                .message(response.status())
                .id((long) response.id())
                .build());
    }

    FilteredValidateOtpResponse validateOtp(OtpValidateRequest request) {
        FlutterwaveResponse response = flutterwaveClient.validateCharge(request);
        helper.handleErrorStatus(response);

        return helper.filterValidateOtpResponse(response);
    }

    // Verify Charge

    public ApiResponse<?> verifyCardTransaction(VerifyChargeCardRequest request) throws Exception {
        CardFundingTransaction cardFundingTransaction = transactionService.getCardFundingTransaction(
                UUID.fromString(request.txn_ref()));

        Transaction transaction = transactionService.getTransaction(cardFundingTransaction.getTransactionId());

        FilteredVerifyChargeCardResponse response;
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

    ApiResponse<?> verifyResponse(FilteredVerifyChargeCardResponse response,
                                  CardFundingTransaction cardFundingTransaction, Transaction transaction) throws Exception {
        return switch (response.data_status()) {
            case "successful" -> {
                setSuccessfulCardFunding(cardFundingTransaction, transaction);
                yield ApiResponse.success(HttpStatus.OK.value(), VerifyChargeCardResponse.builder()
                        .status(response.data_status())
                        .message(response.processor_response())
                        .id((long) response.id())
                        .txn_ref(cardFundingTransaction.getId().toString())
                        .flw_ref(response.flw_ref())
                        .transactionId(transaction.getId().toString())
                        .amount(transaction.getAmount())
                        .currency(transaction.getCurrency())
                        .build());
            }
            case "pending" -> ApiResponse.success(HttpStatus.OK.value(), VerifyChargeCardResponse.builder()
                    .status(response.data_status())
                    .message(response.processor_response())
                    .id((long) response.id())
                    .txn_ref(cardFundingTransaction.getId().toString())
                    .flw_ref(response.flw_ref())
                    .transactionId(transaction.getId().toString())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .build());
            default -> {
                transactionService.setFailedCardFundingTransaction(cardFundingTransaction, transaction, ErrorCode.EXTERNAL_PSP_ERROR);
                yield ApiResponse.success(HttpStatus.OK.value(), VerifyChargeCardResponse.builder()
                        .status(response.data_status())
                        .message(response.processor_response())
                        .id((long) response.id())
                        .txn_ref(cardFundingTransaction.getId().toString())
                        .flw_ref(response.flw_ref())
                        .transactionId(transaction.getId().toString())
                        .amount(transaction.getAmount())
                        .currency(transaction.getCurrency())
                        .build());
            }
        };
    }

    FilteredVerifyChargeCardResponse verifyChargeCard(Long id) {
        FlutterwaveResponse response = flutterwaveClient.verifyCharge(id);
        helper.handleErrorStatus(response);

        return helper.filterVerifyChargeCard(response);
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
}
