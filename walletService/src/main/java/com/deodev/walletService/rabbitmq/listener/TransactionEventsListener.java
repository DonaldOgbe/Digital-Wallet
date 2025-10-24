package com.deodev.walletService.rabbitmq.listener;

import com.deodev.walletService.accountService.service.AccountService;
import com.deodev.walletService.rabbitmq.events.AccountFundedEvent;
import com.deodev.walletService.redis.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Service;
import static com.deodev.walletService.rabbitmq.constants.keys.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionEventsListener {

    private final AccountService accountService;
    private final RedisCacheService redisCacheService;

    @RabbitListener(queues = TRANSACTION_QUEUE)
    public void handleTransactionEvents(
            @Payload AccountFundedEvent event,
            @Header("amqp_receivedRoutingKey") String routingKey
    ) {
        log.info("Received transaction event [{}] for account [{}]", routingKey, event.accountNumber());

        try {
            if (ACCOUNT_FUNDED.equals(routingKey)) {

                boolean firstTime = redisCacheService.setIfAbsent(event.eventId());
                if (!firstTime) {
                    log.warn("Duplicate event [{}] detected. Skipping processing.", event.eventId());
                    return;
                }

                accountService.creditBalance(event.accountNumber(), event.amount());
                log.info("Account [{}] credited with [{}]", event.accountNumber(), event.amount());

            } else {
                log.warn("Unhandled transaction routing key: {}", routingKey);
            }

        } catch (Exception ex) {
            log.error("Error processing transaction event [{}] for account [{}]: {}",
                    routingKey, event.accountNumber(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
