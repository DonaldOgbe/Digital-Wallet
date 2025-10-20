package com.deodev.walletService.rabbitmq.publisher;

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
}
