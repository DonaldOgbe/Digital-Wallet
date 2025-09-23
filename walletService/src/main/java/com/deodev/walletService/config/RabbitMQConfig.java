package com.deodev.walletService.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static com.deodev.walletService.rabbitmq.constants.keys.*;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

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
    public TopicExchange walletEventsExchange() {
        return new TopicExchange(WALLET_EXCHANGE);
    }

    // Transaction Events
    @Bean
    public Queue walletTransactionEventsQueue() {
        return new Queue(WALLET_TRANSACTION_QUEUE, true);
    }

    @Bean
    public TopicExchange transactionEventsExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE);
    }

    @Bean
    public Binding walletTransactionEventsBinding(Queue walletTransactionEventsQueue,
                                                  TopicExchange transactionEventsExchange) {
        return BindingBuilder.bind(walletTransactionEventsQueue)
                .to(transactionEventsExchange)
                .with(TRANSACTION_P2P_WILDCARD);
    }


    // User Events
    @Bean
    public Queue walletUserEventsQueue() {
        return new Queue(WALLET_USER_QUEUE, true);
    }

    @Bean
    public TopicExchange userEventsExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public Binding walletUserEventsBinding(Queue walletUserEventsQueue,
                                           TopicExchange userEventsExchange) {
        return BindingBuilder.bind(walletUserEventsQueue)
                .to(userEventsExchange)
                .with(USER_WILDCARD_KEY);
    }


}
