package br.com.fiap.hacka.uploadservice.app.device.rest.impl;

import br.com.fiap.hacka.uploadservice.app.device.rest.FileUploadApi;
import br.com.fiap.hacka.uploadservice.app.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CommonsLog
@RequiredArgsConstructor
@RestController
public class FileUploadApiImpl implements FileUploadApi {

    private final UploadService uploadService;

    @Operation(summary = "Metodo que faz o upload dos arquivos de video.", method = "POST")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criacao realizada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Objeto invalido.")
    })
    @PostMapping(value = "/upload")
    public ResponseEntity<?> uploadFile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Arquivo MultiFormPart a ser recebido.")
            @RequestParam("file") MultipartFile file,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Arquivo JWT contendo o usuario logado.")
            @RequestHeader("Authorization") String authHeader,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "URL do webhook a ser notificado com o status final do upload.")
            @RequestParam("webhook") String webhook) {
        InputStream stream = null;
        boolean success = false;
        String fileName = file.getOriginalFilename();
        try {
            stream = file.getInputStream();
            success = uploadService.uploadFile(fileName, stream, obterDadosUsuarioJwt(authHeader), webhook);
        } catch (Exception e) {
            log.error("Ocorreu um erro durante o armazenamento do arquivo: " + fileName, e);
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //informa se houve sucesso na leitura do arquivo inteiro. Isso nao garante que o armazenamento seja realizado com sucesso.
        return success ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
    }

    private String obterDadosUsuarioJwt(String authHeader) throws Exception {
        String token = authHeader.replace("Bearer ", "");
        Map<String, Object> claims = decodeJwtPayload(token);
        String username = (String) claims.get("cognito:username");
        String userId = (String) claims.get("sub");

        log.info("Dados do usuário cognito recebidos: ID: " + userId + " Username: " + username);
        return username;
    }

    private Map<String, Object> decodeJwtPayload(String token) throws Exception {
        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        return new ObjectMapper().readValue(payload, Map.class);
    }
}