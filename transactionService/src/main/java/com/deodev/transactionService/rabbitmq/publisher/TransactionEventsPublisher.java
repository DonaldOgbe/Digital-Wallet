package com.deodev.transactionService.rabbitmq.publisher;


import com.deodev.transactionService.rabbitmq.events.AccountFundedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import static com.deodev.transactionService.rabbitmq.constants.keys.*;

@Component
@RequiredArgsConstructor
public class TransactionEventsPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishAccountFunded(AccountFundedEvent event) {
        rabbitTemplate.convertAndSend(TRANSACTION_EXCHANGE, ACCOUNT_FUNDED, event);
    }
}
