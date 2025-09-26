package com.deodev.walletService.rabbitmq.listener;

import com.deodev.walletService.accountService.dto.response.TransferFundsResponse;
import com.deodev.walletService.accountService.service.AccountService;
import com.deodev.walletService.enums.ErrorCode;
import com.deodev.walletService.exception.FundReservationException;
import com.deodev.walletService.exception.ResourceNotFoundException;
import com.deodev.walletService.rabbitmq.events.P2PTransferRequestedEvent;
import com.deodev.walletService.rabbitmq.publisher.WalletEventsPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.deodev.walletService.rabbitmq.constants.keys.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionEventsListener {

    private final AccountService accountService;
    private final WalletEventsPublisher walletEventsPublisher;

    @RabbitListener(queues = WALLET_TRANSACTION_QUEUE)
    public void handleP2PTransferRequested(
            @Payload P2PTransferRequestedEvent event,
            @Header("amqp_receivedRoutingKey") String routingKey
            ) {
        log.info("Transaction Event received: {}, transactionId: {}", routingKey, event.transactionId());

        switch (routingKey) {
            case P2P_TRANSFER_REQUESTED -> {
                try {
                    TransferFundsResponse response = accountService.transferFunds(event);
                    log.info("P2P transfer processed successfully: transactionId={}, fundReservationId={}",
                            response.transactionId(), response.fundReservationId());

                } catch (ResourceNotFoundException e) {
                    handleException(e.getMessage().isEmpty() ? "Resource not found" : e.getMessage(),
                            e.getErrorCode(), event.transactionId(), e);
                } catch (FundReservationException e) {
                    handleException(e.getMessage().isEmpty() ? "Fund reservation error" : e.getMessage(),
                            e.getErrorCode(), event.transactionId(), e);
                } catch (Exception e) {
                    handleException(e.getMessage().isEmpty() ? "Unexpected error" : e.getMessage(),
                            ErrorCode.SYSTEM_ERROR, event.transactionId(), e);
                }
            }
            default -> log.warn("Unknown transaction routing key: {}", routingKey);
        }
    }

    void handleException(String message, ErrorCode errorCode, UUID transactionId, Exception e) {
        log.error("Failed to process P2P transfer for transactionId={}: {}, {}", transactionId, message, errorCode, e);
        walletEventsPublisher.publishTransferFailed(transactionId, errorCode, message);
    }
}
