package com.deodev.userService.rabbitmq.publisher;

import com.deodev.userService.model.User;
import com.deodev.userService.rabbitmq.event.UserRegisteredEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;


import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventsPublisherTest {

    private static final String USER_EXCHANGE = "user.exchange";
    private static final String USER_CREATED = "user.created";

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserEventsPublisher userEventsPublisher;


    @Test
    void publishUserRegistered_SendsCorrectEvent() {
        // given
        User user = User.builder().id(UUID.randomUUID()).build();

        ArgumentCaptor<UserRegisteredEvent> captor = ArgumentCaptor.forClass(UserRegisteredEvent.class);

        // when
        userEventsPublisher.publishUserRegistered(user);

        // then
        verify(rabbitTemplate).convertAndSend(
                eq(USER_EXCHANGE),
                eq(USER_CREATED),
                captor.capture()
        );

        UserRegisteredEvent event = captor.getValue();
        assertEquals(event.userid(), user.getId());
    }

}