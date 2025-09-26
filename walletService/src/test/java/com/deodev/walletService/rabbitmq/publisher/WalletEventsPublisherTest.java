package com.deodev.walletService.rabbitmq.publisher;

import com.deodev.walletService.enums.ErrorCode;
import com.deodev.walletService.rabbitmq.events.TransferCompletedEvent;
import com.deodev.walletService.rabbitmq.events.TransferFailedEvent;
import com.deodev.walletService.rabbitmq.events.WalletCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static com.deodev.walletService.rabbitmq.constants.keys.*;


@ExtendWith(MockitoExtension.class)
class WalletEventsPublisherTest {
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private WalletEventsPublisher walletEventsPublisher;

    @Test
    void publishWalletCreated_ShouldPublishEvent() {
        // given
        UUID userId = UUID.randomUUID();

        ArgumentCaptor<WalletCreatedEvent> captor = ArgumentCaptor.forClass(WalletCreatedEvent.class);

        // when
        walletEventsPublisher.publishWalletCreated(userId);

        // then
        verify(rabbitTemplate).convertAndSend(
                eq(WALLET_EXCHANGE),
                eq(WALLET_CREATED),
                captor.capture()
        );

        assertThat(captor.getValue().userId()).isEqualTo(userId);
    }

    @Test
    void publishTransferCompleted_ShouldPublishEvent() {
        // given
        UUID transactionId = UUID.randomUUID();
        UUID fundReservationId = UUID.randomUUID();
        ArgumentCaptor<TransferCompletedEvent> captor = ArgumentCaptor.forClass(TransferCompletedEvent.class);

        // when
        walletEventsPublisher.publishTransferCompleted(transactionId, fundReservationId);

        // then
        verify(rabbitTemplate).convertAndSend(
                eq(WALLET_EXCHANGE),
                eq(TRANSFER_COMPLETED),
                captor.capture()
        );

        TransferCompletedEvent event = captor.getValue();
        assertThat(event.transactionId()).isEqualTo(transactionId);
        assertThat(event.fundReservationId()).isEqualTo(fundReservationId);
    }

    @Test
    void publishTransferFailed_ShouldPublishEvent() {
        // given
        UUID transactionId = UUID.randomUUID();
        ErrorCode errorCode = ErrorCode.SYSTEM_ERROR;
        String message = "Insufficient funds";

        ArgumentCaptor<TransferFailedEvent> captor = ArgumentCaptor.forClass(TransferFailedEvent.class);

        // when
        walletEventsPublisher.publishTransferFailed(transactionId, errorCode, message);

        // then
        verify(rabbitTemplate).convertAndSend(
                eq(WALLET_EXCHANGE),
                eq(TRANSFER_FAILED),
                captor.capture()
        );

        TransferFailedEvent event = captor.getValue();
        assertThat(event.transactionId()).isEqualTo(transactionId);
        assertThat(event.errorCode()).isEqualTo(errorCode);
        assertThat(event.message()).isEqualTo(message);
    }
}