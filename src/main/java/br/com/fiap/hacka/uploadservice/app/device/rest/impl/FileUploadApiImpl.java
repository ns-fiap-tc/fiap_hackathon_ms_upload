package br.com.fiap.hacka.uploadservice.app.device.rest.impl;

import br.com.fiap.hacka.uploadservice.app.device.facade.UploadFacade;
import br.com.fiap.hacka.uploadservice.app.device.rest.FileUploadApi;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CommonsLog
@RequiredArgsConstructor
@RestController
public class FileUploadApiImpl implements FileUploadApi {

    private final UploadFacade uploadFacade;

    @PostMapping(value = "/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        InputStream stream = null;
        boolean success = false;
        String fileName = file.getOriginalFilename();
        try {
            stream = file.getInputStream();
            success = uploadFacade.uploadFile(fileName, stream);
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