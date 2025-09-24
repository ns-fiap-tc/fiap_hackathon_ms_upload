package br.com.fiap.hacka.uploadservice.app.queue.impl;

import br.com.fiap.hacka.core.commons.dto.FilePartDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMqMessageProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private RabbitMqMessageProducer messageProducer;

    @Test
    void shouldSendMessageToQueue() {
        FilePartDto filePartDto = new FilePartDto();
        filePartDto.setFileName("test.mp4");
        filePartDto.setBytesRead(100);
        filePartDto.setBytes("test content".getBytes());
        
        messageProducer.send("test-queue", filePartDto);
        
        verify(rabbitTemplate).convertAndSend("test-queue", filePartDto);
    }

    @Test
    void shouldSendMultipleMessages() {
        FilePartDto firstChunk = new FilePartDto();
        firstChunk.setFileName("test.mp4");
        firstChunk.setFirstChunk(true);
        
        FilePartDto secondChunk = new FilePartDto();
        secondChunk.setFileName("test.mp4");
        secondChunk.setBytesRead(-1);
        
        messageProducer.send("test-queue", firstChunk);
        messageProducer.send("test-queue", secondChunk);
        
        verify(rabbitTemplate, times(2)).convertAndSend(eq("test-queue"), any(FilePartDto.class));
        verify(rabbitTemplate).convertAndSend("test-queue", firstChunk);
        verify(rabbitTemplate).convertAndSend("test-queue", secondChunk);
    }

//    @Test
//    void shouldHandleNullFilePartDto() {
//        messageProducer.send("test-queue", null);
//
//        verify(rabbitTemplate).convertAndSend((Object) "test-queue", null);
//    }

    @Test
    void shouldHandleEmptyQueueName() {
        FilePartDto filePartDto = new FilePartDto();
        
        messageProducer.send("", filePartDto);
        
        verify(rabbitTemplate).convertAndSend("", filePartDto);
    }
}