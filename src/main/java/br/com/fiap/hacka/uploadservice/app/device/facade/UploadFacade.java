package br.com.fiap.hacka.uploadservice.app.device.facade;

import br.com.fiap.hacka.uploadservice.core.commons.FilePart;
import br.com.fiap.hacka.uploadservice.device.queue.MessageProducer;
import br.com.fiap.hacka.uploadservice.infra.config.RabbitMqConfig;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Service;

@CommonsLog
@RequiredArgsConstructor
@Service
public class UploadFacade {
    private final MessageProducer messageProducer;

    public boolean uploadFile(String fileName, InputStream inStream) throws IOException, InterruptedException {
        byte[] buffer = new byte[FilePart.CHUNK_SIZE]; // 1MB chunks
        int bytesRead;
        while ((bytesRead = inStream.read(buffer)) != -1) {
            log.info("arquivo a ser processado: " + fileName);
            messageProducer.send(RabbitMqConfig.QUEUE_NAME, new FilePart(fileName, bytesRead, Arrays.copyOf(buffer, bytesRead)));
            //sleep para sincronizar a execucao.  sem isso, os arquivos perdem a ordem pela execucao ser muito rapida.
            Thread.sleep(2);
        }

        // Send EOF marker
        Thread.sleep(2);
        FilePart end = new FilePart(fileName, -1, new byte[0]);
        log.info("Mandando o EOF do arquivo: " + fileName);
        messageProducer.send(RabbitMqConfig.QUEUE_NAME, end);
        return true;
    }
}