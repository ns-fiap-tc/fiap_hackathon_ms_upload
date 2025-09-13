package br.com.fiap.hacka.uploadservice.infra.config;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

@CommonsLog
@EnableRabbit
@Configuration
public class RabbitMqConfig {//implements RabbitListenerConfigurer {

    public static final String QUEUE_NAME = "uploadS3";

    private final ConnectionFactory connectionFactory;

    public RabbitMqConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    // JSON converter for both producer and consumer
    @Bean
    public MessageConverter jackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate for sending messages
    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2MessageConverter());
        return template;
    }

    // RabbitAdmin for auto-declaring queues/exchanges
    @Bean
    public RabbitAdmin rabbitAdmin() {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        // Declare queue automatically at startup
        admin.declareQueue(new Queue(QUEUE_NAME, true));
        return admin;
    }

    // Listener container factory
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2MessageConverter());
        factory.setPrefetchCount(1);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(5);
        return factory;
    }
}