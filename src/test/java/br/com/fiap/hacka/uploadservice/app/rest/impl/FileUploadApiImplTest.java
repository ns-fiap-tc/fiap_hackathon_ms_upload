package br.com.fiap.hacka.uploadservice.app.rest.impl;

import br.com.fiap.hacka.uploadservice.app.service.UploadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadApiImplTest {

    @Mock
    private UploadService uploadService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private FileUploadApiImpl fileUploadApi;

    @Test
    void shouldUploadFileSuccessfully() throws Exception {
        String jwtToken = createValidJwtToken();
        String authHeader = "Bearer " + jwtToken;
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        
        when(multipartFile.getOriginalFilename()).thenReturn("test.mp4");
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(uploadService.uploadFile(eq("test.mp4"), any(InputStream.class), eq("testuser"), eq("http://webhook.com")))
            .thenReturn(true);
        
        ResponseEntity<?> response = fileUploadApi.uploadFile(multipartFile, authHeader, "http://webhook.com");
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(uploadService).uploadFile(eq("test.mp4"), any(InputStream.class), eq("testuser"), eq("http://webhook.com"));
    }

    @Test
    void shouldReturnExpectationFailedWhenUploadFails() throws Exception {
        String jwtToken = createValidJwtToken();
        String authHeader = "Bearer " + jwtToken;
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        
        when(multipartFile.getOriginalFilename()).thenReturn("test.mp4");
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(uploadService.uploadFile(any(), any(), any(), any())).thenReturn(false);
        
        ResponseEntity<?> response = fileUploadApi.uploadFile(multipartFile, authHeader, "http://webhook.com");
        
        assertEquals(HttpStatus.EXPECTATION_FAILED, response.getStatusCode());
    }

    @Test
    void shouldHandleUploadServiceException() throws Exception {
        String jwtToken = createValidJwtToken();
        String authHeader = "Bearer " + jwtToken;
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());
        
        when(multipartFile.getOriginalFilename()).thenReturn("test.mp4");
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(uploadService.uploadFile(any(), any(), any(), any())).thenThrow(new IOException("Upload failed"));
        
        assertThrows(RuntimeException.class, () -> 
            fileUploadApi.uploadFile(multipartFile, authHeader, "http://webhook.com"));
    }

    @Test
    void shouldHandleInputStreamException() throws Exception {
        String jwtToken = createValidJwtToken();
        String authHeader = "Bearer " + jwtToken;
        
        when(multipartFile.getOriginalFilename()).thenReturn("test.mp4");
        when(multipartFile.getInputStream()).thenThrow(new IOException("Stream error"));
        
        assertThrows(RuntimeException.class, () -> 
            fileUploadApi.uploadFile(multipartFile, authHeader, "http://webhook.com"));
    }

    @Test
    void shouldHandleInvalidJwtToken() {
        String invalidAuthHeader = "Bearer invalid.token.here";
        
        when(multipartFile.getOriginalFilename()).thenReturn("test.mp4");
        
        assertThrows(RuntimeException.class, () -> 
            fileUploadApi.uploadFile(multipartFile, invalidAuthHeader, "http://webhook.com"));
    }

    @Test
    void shouldCloseInputStreamOnSuccess() throws Exception {
        String jwtToken = createValidJwtToken();
        String authHeader = "Bearer " + jwtToken;
        InputStream mockInputStream = mock(InputStream.class);
        
        when(multipartFile.getOriginalFilename()).thenReturn("test.mp4");
        when(multipartFile.getInputStream()).thenReturn(mockInputStream);
        when(uploadService.uploadFile(any(), any(), any(), any())).thenReturn(true);
        
        fileUploadApi.uploadFile(multipartFile, authHeader, "http://webhook.com");
        
        verify(mockInputStream).close();
    }

    @Test
    void shouldCloseInputStreamOnException() throws Exception {
        String jwtToken = createValidJwtToken();
        String authHeader = "Bearer " + jwtToken;
        InputStream mockInputStream = mock(InputStream.class);
        
        when(multipartFile.getOriginalFilename()).thenReturn("test.mp4");
        when(multipartFile.getInputStream()).thenReturn(mockInputStream);
        when(uploadService.uploadFile(any(), any(), any(), any())).thenThrow(new RuntimeException("Error"));
        
        assertThrows(RuntimeException.class, () -> 
            fileUploadApi.uploadFile(multipartFile, authHeader, "http://webhook.com"));
        
        verify(mockInputStream).close();
    }

    private String createValidJwtToken() {
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"cognito:username\":\"testuser\"}".getBytes());
        String signature = Base64.getUrlEncoder().withoutPadding().encodeToString("signature".getBytes());
        return header + "." + payload + "." + signature;
    }
}