package br.com.fiap.hacka.uploadservice.device.queue.impl;

import br.com.fiap.hacka.uploadservice.core.commons.FilePart;
import br.com.fiap.hacka.uploadservice.device.queue.MessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RabbitMqMessageProducer implements MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void send(String queueName, FilePart filePart) {
        rabbitTemplate.convertAndSend(queueName, filePart);
    }
}