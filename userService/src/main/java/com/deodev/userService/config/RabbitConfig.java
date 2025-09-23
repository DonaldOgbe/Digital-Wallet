package com.deodev.userService.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.management.Query;

import static com.deodev.userService.rabbitmq.constants.keys.*;

@Configuration
@EnableRabbit
public class RabbitConfig {

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    // WALLET EVENTS

    @Bean
    public Queue userWalletEventsQueue() {
        return new Queue(USER_WALLET_QUEUE, true);
    }

    @Bean
    public TopicExchange walletEventsExchange() {
        return new TopicExchange(WALLET_EXCHANGE);
    }

    @Bean
    public Binding userWalletEventsBinding(Queue userWalletEventsQueue,
                                           TopicExchange walletEventsExchange) {
        return BindingBuilder.bind(userWalletEventsQueue)
                .to(walletEventsExchange)
                .with(WALLET_WILDCARD_KEY);
    }
}
