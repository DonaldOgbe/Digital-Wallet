package com.deodev.transactionService.rabbitmq.publisher;


import com.deodev.transactionService.rabbitmq.events.AccountFundedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import static com.deodev.transactionService.rabbitmq.constants.keys.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventsPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishAccountFunded(AccountFundedEvent event) {
        rabbitTemplate.convertAndSend(TRANSACTION_EXCHANGE, ACCOUNT_FUNDED, event);
        log.info("Account funded event published, eventId: {}, account number: {}, amount: {}",
                event.eventId(), event.accountNumber(), event.amount());
    }
}
