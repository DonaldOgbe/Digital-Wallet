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

    @RabbitListener(queues = USER_QUEUE)
    public void handleUserEvents(
            @Payload UserRegisteredEvent event,
            @Header("amqp_receivedRoutingKey") String routingKey
    ) {
        log.info("User Registration Event: {}, userId: {}", routingKey, event.userid());

        try {
            if (routingKey.equals(USER_CREATED)) {
                walletService.createWallet(String.valueOf(event.userid()));
                log.info("Wallet created for user [{}]", event.userid());
            } else {
                log.warn("Unknown user routing key: {}", routingKey);
            }

        } catch (Exception ex) {
            log.error("Error processing user event [{}] for user [{}]: {}",
                    routingKey, event.userid(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
