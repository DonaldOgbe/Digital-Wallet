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

    @RabbitListener(queues = WALLET_QUEUE)
    public void handleWalletLifecycleEvents(
            @Payload WalletCreatedEvent event,
            @Header("amqp_receivedRoutingKey") String routingKey
    ) {
        log.info("Received wallet event [{}] for user [{}]", routingKey, event.userId());

        try {
            if (WALLET_CREATED.equals(routingKey)) {
                userService.markUserVerified(event.userId());
                log.info("User [{}] marked as verified after wallet creation", event.userId());
            } else {
                log.warn("Unhandled wallet routing key: {}", routingKey);
            }
        } catch (Exception ex) {
            log.error("Error processing wallet event [{}] for user [{}]: {}",
                    routingKey, event.userId(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
