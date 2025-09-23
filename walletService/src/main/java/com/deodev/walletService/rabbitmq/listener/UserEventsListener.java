package com.deodev.walletService.rabbitmq.listener;

import com.deodev.walletService.rabbitmq.events.UserRegisteredEvent;
import com.deodev.walletService.walletService.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Service;
import static com.deodev.walletService.rabbitmq.constants.keys.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventsListener {

    private final WalletService walletService;

    @RabbitListener(queues = WALLET_USER_QUEUE)
    public void handleUserRegistrationEvents(
            @Payload UserRegisteredEvent event,
            @Header("amqp_receivedRoutingKey") String routingKey
    ) {
        log.info("User Registration Event: {}, userId: {}", routingKey, event.userid());

        switch (routingKey) {
            case USER_CREATED -> walletService.createWallet(String.valueOf(event.userid()));
            default -> log.warn("Unknown user routing key: {}", routingKey);
        }
    }
}
