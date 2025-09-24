package br.com.fiap.hacka.core.commons.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FilePartDtoTest {

    @Test
    void shouldCreateFilePartDtoWithAllArgsConstructor() {
        byte[] testBytes = "test content".getBytes();
        FilePartDto dto = new FilePartDto("test.mp4", 12, testBytes, true, "user1", "http://webhook.com", "/path/frame.jpg", "http://file.url");
        
        assertEquals("test.mp4", dto.getFileName());
        assertEquals(12, dto.getBytesRead());
        assertArrayEquals(testBytes, dto.getBytes());
        assertTrue(dto.isFirstChunk());
        assertEquals("user1", dto.getUserName());
        assertEquals("http://webhook.com", dto.getWebhookUrl());
        assertEquals("/path/frame.jpg", dto.getFrameFilePath());
        assertEquals("http://file.url", dto.getFileUrl());
    }

    @Test
    void shouldCreateFilePartDtoWithNoArgsConstructor() {
        FilePartDto dto = new FilePartDto();
        
        assertNull(dto.getFileName());
        assertEquals(0, dto.getBytesRead());
        assertNull(dto.getBytes());
        assertFalse(dto.isFirstChunk());
        assertNull(dto.getUserName());
        assertNull(dto.getWebhookUrl());
        assertNull(dto.getFrameFilePath());
        assertNull(dto.getFileUrl());
    }

    @Test
    void shouldSetAndGetAllFields() {
        FilePartDto dto = new FilePartDto();
        byte[] testBytes = "content".getBytes();
        
        dto.setFileName("video.mp4");
        dto.setBytesRead(7);
        dto.setBytes(testBytes);
        dto.setFirstChunk(true);
        dto.setUserName("testUser");
        dto.setWebhookUrl("http://test.webhook");
        dto.setFrameFilePath("/frames/test.jpg");
        dto.setFileUrl("http://files/video.mp4");
        
        assertEquals("video.mp4", dto.getFileName());
        assertEquals(7, dto.getBytesRead());
        assertArrayEquals(testBytes, dto.getBytes());
        assertTrue(dto.isFirstChunk());
        assertEquals("testUser", dto.getUserName());
        assertEquals("http://test.webhook", dto.getWebhookUrl());
        assertEquals("/frames/test.jpg", dto.getFrameFilePath());
        assertEquals("http://files/video.mp4", dto.getFileUrl());
    }

    @Test
    void shouldHaveCorrectChunkSize() {
        assertEquals(1024 * 1024, FilePartDto.CHUNK_SIZE);
    }
}