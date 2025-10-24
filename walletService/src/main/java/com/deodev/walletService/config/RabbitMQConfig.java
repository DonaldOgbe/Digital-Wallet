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

    // DLX

    @Bean
    public DirectExchange dlx() {
        return new DirectExchange(DLX);
    }

    // Wallet

    @Bean
    public TopicExchange walletExchange() {
        return new TopicExchange(WALLET_EXCHANGE);
    }

    // Transaction Events

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE);
    }

    @Bean
    public Queue transactionQueue() {
        return QueueBuilder.durable(TRANSACTION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", TRANSACTION_DLQ)
                .build();
    }

    @Bean
    public Binding transactionBinding(Queue transactionQueue,
                                      TopicExchange transactionExchange) {
        return BindingBuilder.bind(transactionQueue)
                .to(transactionExchange)
                .with(TRANSACTION_WILDCARD);
    }

    @Bean
    public Queue transactionDlq() {
        return new Queue(TRANSACTION_DLQ, true);
    }

    @Bean
    public Binding transactionDlBinding(Queue transactionDlq,
                                        DirectExchange dlx) {
        return BindingBuilder.bind(transactionDlq)
                .to(dlx)
                .with(TRANSACTION_DLQ);
    }

    // User Events

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue userQueue() {
        return QueueBuilder.durable(USER_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", USER_DLQ)
                .build();
    }

    @Bean
    public Binding userBinding(Queue userQueue,
                               TopicExchange userExchange) {
        return BindingBuilder.bind(userQueue)
                .to(userExchange)
                .with(USER_WILDCARD);
    }


    @Bean
    public Queue userDlq() {
        return new Queue(USER_DLQ, true);
    }

    @Bean
    public Binding userDlBinding(Queue userDlq,
                                        DirectExchange dlx) {
        return BindingBuilder.bind(userDlq)
                .to(dlx)
                .with(USER_DLQ);
    }

}
