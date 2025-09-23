package com.deodev.walletService.rabbitmq.publisher;

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
}