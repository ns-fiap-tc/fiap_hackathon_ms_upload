package br.com.fiap.hacka.uploadservice.app.rest.impl;

import br.com.fiap.hacka.uploadservice.app.rest.FileUploadApi;
import br.com.fiap.hacka.uploadservice.app.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CommonsLog
@RequiredArgsConstructor
@RestController
public class FileUploadApiImpl implements FileUploadApi {

    private final UploadService uploadService;

    @Operation(summary = "Recebe os arquivos que terao os frames extraidos. O request deve ser do tipo MultipartFile.", method = "POST")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recebimento realizado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Objeto invalido.")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "objeto a ser criado.")
            @RequestParam("file") MultipartFile file,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "nome do usuario que fez o upload do arquivo.")
            @RequestParam("userName") String userName,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "web hook para onde sera enviada a notificacao sobre a conclusao do upload do arquivo (sucesso ou facasso)")
            @RequestParam("webhook") String webhook)
    {
        InputStream stream = null;
        boolean success = false;
        String fileName = file.getOriginalFilename();
        try {
            stream = file.getInputStream();
            success = uploadService.uploadFile(fileName, stream, userName, webhook);
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
}