package com.deodev.userService.rabbitmq.publisher;

import com.deodev.userService.rabbitmq.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import static com.deodev.userService.rabbitmq.constants.keys.*;

@Service
@RequiredArgsConstructor
public class UserEventsPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserRegistered(UserRegisteredEvent event) {
        rabbitTemplate.convertAndSend(USER_EXCHANGE, USER_REGISTERED_ROUTING_KEY, event);
    }
}
