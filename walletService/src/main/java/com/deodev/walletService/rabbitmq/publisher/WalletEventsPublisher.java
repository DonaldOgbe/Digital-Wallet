package com.deodev.walletService.rabbitmq.publisher;

import com.deodev.walletService.enums.ErrorCode;
import com.deodev.walletService.rabbitmq.events.TransferCompletedEvent;
import com.deodev.walletService.rabbitmq.events.TransferFailedEvent;
import com.deodev.walletService.rabbitmq.events.WalletCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import static com.deodev.walletService.rabbitmq.constants.keys.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletEventsPublisher {
    private final RabbitTemplate rabbitTemplate;

    public void publishWalletCreated(UUID userid) {
        WalletCreatedEvent event = new WalletCreatedEvent(userid);
        rabbitTemplate.convertAndSend(WALLET_EXCHANGE, WALLET_CREATED, event);
    }

    public void publishTransferCompleted(UUID transactionId, UUID fundReservationId) {
        TransferCompletedEvent event = new TransferCompletedEvent(transactionId, fundReservationId);
        rabbitTemplate.convertAndSend(WALLET_EXCHANGE, TRANSFER_COMPLETED, event);
    }

    public void publishTransferFailed(UUID transactionId, ErrorCode errorCode, String message) {
        TransferFailedEvent event = new TransferFailedEvent(transactionId, errorCode, message);
        rabbitTemplate.convertAndSend(WALLET_EXCHANGE, TRANSFER_FAILED, event);
    }
}
