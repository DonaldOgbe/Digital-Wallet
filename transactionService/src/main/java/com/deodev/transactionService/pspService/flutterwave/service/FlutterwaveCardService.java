package com.deodev.transactionService.pspService.flutterwave.service;

import com.deodev.transactionService.dto.ApiResponse;
import com.deodev.transactionService.dto.ErrorResponse;
import com.deodev.transactionService.enums.ErrorCode;
import com.deodev.transactionService.enums.PaymentGateway;
import com.deodev.transactionService.enums.TransactionStatus;
import com.deodev.transactionService.enums.TransactionType;
import com.deodev.transactionService.exception.PSPException;
import com.deodev.transactionService.pspService.flutterwave.client.FlutterwaveClient;
import com.deodev.transactionService.pspService.flutterwave.dto.ChargeCardPayload;
import com.deodev.transactionService.pspService.flutterwave.dto.request.EncryptedChargeRequest;
import com.deodev.transactionService.pspService.flutterwave.dto.request.InitiateChargeCardRequest;
import com.deodev.transactionService.pspService.flutterwave.dto.response.CardTypePayload;
import com.deodev.transactionService.pspService.flutterwave.dto.response.InitiateChargeCardResponse;
import com.deodev.transactionService.pspService.walletService.service.WalletService;
import com.deodev.transactionService.redis.RedisCacheService;
import com.deodev.transactionService.transactionService.model.CardFundingTransaction;
import com.deodev.transactionService.transactionService.model.Transaction;
import com.deodev.transactionService.transactionService.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    public ApiResponse<?> getCardType(String bin) {
        Map<String, Object> response = flutterwaveClient.resolveCard(bin);
        handleErrorStatus(response, "Flutterwave failed to resolve card type for BIN " + bin);

        Map<String, Object> data = (Map<String, Object>) response.get("data");
        String cardType = (data != null) ? (String) data.getOrDefault("card_type", "UNKNOWN") : "UNKNOWN";

        return ApiResponse.success(HttpStatus.OK.value(), new CardTypePayload(cardType));
    }

    public ApiResponse<?> initiateChargeCard(InitiateChargeCardRequest request,
                                             String userId, String idempotencyKey) throws IOException {
        if (!walletService.verifyAccountNumber(request.accountNumber(), request.currency())) {
            return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), ErrorCode.NOT_FOUND,
                    ErrorResponse.builder().message("Account number %s does not exists".formatted(request.accountNumber())));
        }

        Transaction transaction = createNewTransaction(request, userId, TransactionType.CARD_FUND, idempotencyKey);
        CardFundingTransaction cardFundingTransaction = createNewCardFundingTransaction(request, transaction.getId());

        Map<String, Object> response = new HashMap<>();
        try {
            response = chargeCard(request.client());
        } catch (Exception ex) {
            transactionService.setFailedCardFundingTransaction(cardFundingTransaction, transaction, ErrorCode.EXTERNAL_PSP_ERROR);
            log.error("Transaction {} failed due to exception", transaction.getId(), ex);
            throw ex;
        }

        CardFundingTransaction savedCardFundingTransaction = transactionService.getCardFundingTransaction(cardFundingTransaction.getTransactionId());
        cardFundingTransaction.setGatewayTransactionId((String) response.get("id"));
        cardFundingTransaction.setGatewayReference((String) response.get("flw_ref"));
        cardFundingTransaction.setAuthorizationCode((String) response.get("mode"));
        transactionService.saveCardFundingTransaction(cardFundingTransaction);
        Map<String, Object> auth = (Map<String, Object>) response.get("authorization");

        return ApiResponse.success(HttpStatus.OK.value(), InitiateChargeCardResponse.builder()
                .txn_ref(transaction.getId().toString())
                .id((String) response.get("id"))
                .mode((String) response.get("mode"))
                .flw_ref((String) response.get("flw_ref"))
                .redirect(auth != null ? (String) auth.get("redirect") : null)
                .message((String) response.get("processor_response"))
                .build());
    }

    Map<String, Object> chargeCard(String client) throws IOException {
        EncryptedChargeRequest chargeRequest = new EncryptedChargeRequest(client);

        Map<String, Object> response = flutterwaveClient.chargeCard(chargeRequest);
        handleErrorStatus(response, "Flutterwave failed to initiate charge card");

        return filterChargeCardResponse(response);
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
