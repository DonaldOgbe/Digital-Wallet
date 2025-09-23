package com.deodev.userService.rabbitmq.listener;

import com.deodev.userService.rabbitmq.event.WalletCreatedEvent;
import com.deodev.userService.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import static com.deodev.userService.rabbitmq.constants.keys.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletEventsListener {

    private final UserService userService;

    @RabbitListener(queues = USER_WALLET_QUEUE)
    public void handleWalletLifecycleEvents(
            @Payload WalletCreatedEvent event,
            @Header("amqp_receivedRoutingKey") String routingKey
    ) {
        log.info("Wallet Lifecycle Event: {}, userId: {}",
                routingKey, event.userId());

        switch (routingKey) {
            case WALLET_CREATED -> {
                userService.markUserVerified(event.userId());
                log.info("User [{}] marked as verified after wallet creation", event.userId());
            }
            default -> log.warn("Unknown wallet routing key: {}", routingKey);
        }
    }
}
