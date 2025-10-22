package com.deodev.userService.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    // DLX

    @Bean
    public DirectExchange dlx() {
        return new DirectExchange(DLX);
    }

    // User

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    // Wallet Events

    @Bean
    public TopicExchange walletExchange() {
        return new TopicExchange(WALLET_EXCHANGE);
    }

    @Bean
    public Queue walletQueue() {
        return  QueueBuilder.durable(WALLET_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", WALLET_DLQ)
                .build();
    }

    @Bean
    public Binding userWalletEventsBinding(Queue walletQueue,
                                           TopicExchange walletExchange) {
        return BindingBuilder.bind(walletQueue)
                .to(walletExchange)
                .with(WALLET_WILDCARD);
    }
}
