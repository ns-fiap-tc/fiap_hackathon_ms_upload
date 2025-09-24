package br.com.fiap.hacka.uploadservice.app.service;

import br.com.fiap.hacka.core.commons.dto.FilePartDto;
import br.com.fiap.hacka.uploadservice.app.queue.MessageProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

    @Mock
    private MessageProducer messageProducer;

    @InjectMocks
    private UploadService uploadService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(uploadService, "queueName", "test-queue");
    }

    @Test
    void shouldUploadFileSuccessfully() throws IOException, InterruptedException {
        byte[] fileContent = "test file content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);
        
        boolean result = uploadService.uploadFile("test.mp4", inputStream, "testUser", "http://webhook.com");
        
        assertTrue(result);
        verify(messageProducer, times(2)).send(eq("test-queue"), any(FilePartDto.class));
    }

    @Test
    void shouldSendFirstChunkWithCorrectFlag() throws IOException, InterruptedException {
        byte[] fileContent = "test content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);
        ArgumentCaptor<FilePartDto> captor = ArgumentCaptor.forClass(FilePartDto.class);
        
        uploadService.uploadFile("test.mp4", inputStream, "testUser", "http://webhook.com");
        
        verify(messageProducer, times(2)).send(eq("test-queue"), captor.capture());
        FilePartDto firstChunk = captor.getAllValues().get(0);
        assertTrue(firstChunk.isFirstChunk());
        assertEquals("test.mp4", firstChunk.getFileName());
        assertEquals("testUser", firstChunk.getUserName());
        assertEquals("http://webhook.com", firstChunk.getWebhookUrl());
    }

    @Test
    void shouldSendEofMarker() throws IOException, InterruptedException {
        byte[] fileContent = "test".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);
        ArgumentCaptor<FilePartDto> captor = ArgumentCaptor.forClass(FilePartDto.class);
        
        uploadService.uploadFile("test.mp4", inputStream, "testUser", "http://webhook.com");
        
        verify(messageProducer, times(2)).send(eq("test-queue"), captor.capture());
        FilePartDto eofMarker = captor.getAllValues().get(1);
        assertEquals(-1, eofMarker.getBytesRead());
        assertEquals(0, eofMarker.getBytes().length);
        assertEquals("test.mp4", eofMarker.getFileName());
    }

    @Test
    void shouldHandleLargeFile() throws IOException, InterruptedException {
        byte[] largeContent = new byte[FilePartDto.CHUNK_SIZE * 2 + 100];
        InputStream inputStream = new ByteArrayInputStream(largeContent);
        
        uploadService.uploadFile("large.mp4", inputStream, "testUser", "http://webhook.com");
        
        verify(messageProducer, times(4)).send(eq("test-queue"), any(FilePartDto.class));
    }

    @Test
    void shouldHandleEmptyFile() throws IOException, InterruptedException {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        
        uploadService.uploadFile("empty.mp4", inputStream, "testUser", "http://webhook.com");
        
        verify(messageProducer, times(1)).send(eq("test-queue"), any(FilePartDto.class));
    }

    @Test
    void shouldPropagateIOException() {
        InputStream inputStream = mock(InputStream.class);
        try {
            when(inputStream.read(any(byte[].class))).thenThrow(new IOException("Read error"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        assertThrows(IOException.class, () -> 
            uploadService.uploadFile("test.mp4", inputStream, "testUser", "http://webhook.com"));
    }
}