package br.com.fiap.hacka.uploadservice.app.service;

import br.com.fiap.hacka.core.commons.dto.FilePartDto;
import br.com.fiap.hacka.uploadservice.app.queue.MessageProducer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@CommonsLog
@RequiredArgsConstructor
@Service
public class UploadService {

    private final MessageProducer messageProducer;

    @Value("${rabbitmq.queue.producer.messageQueue}")
    private String queueName;

    public boolean uploadFile(String fileName, InputStream inStream, String userName, String webhookUrl) throws IOException, InterruptedException {
        byte[] buffer = new byte[FilePartDto.CHUNK_SIZE]; // 1MB chunks
        int bytesRead;
        boolean isFirstChunk = true;

        while ((bytesRead = inStream.read(buffer)) != -1) {
            log.info("arquivo a ser processado: " + fileName);
            FilePartDto dto = new FilePartDto();
            if (isFirstChunk) {
                dto.setFirstChunk(isFirstChunk);
            }
            dto.setFileName(fileName);
            dto.setBytes(Arrays.copyOf(buffer, bytesRead));
            dto.setBytesRead(bytesRead);
            dto.setUserName(userName);
            dto.setWebhookUrl(webhookUrl);
            messageProducer.send(queueName, dto);
            isFirstChunk = false;
            //sleep para sincronizar a execucao.  sem isso, os arquivos perdem a ordem pela execucao ser muito rapida.
            Thread.sleep(2);
        }

        // Send EOF marker
        Thread.sleep(2);
        FilePartDto end = new FilePartDto();
        end.setFileName(fileName);
        end.setBytes(new byte[0]);
        end.setBytesRead(-1);
        end.setUserName(userName);
        end.setWebhookUrl(webhookUrl);
        log.info("Mandando o EOF do arquivo: " + fileName);
        messageProducer.send(queueName, end);
        return true;
    }
}