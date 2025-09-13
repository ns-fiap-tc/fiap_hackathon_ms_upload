package br.com.fiap.hacka.uploadservice.device.queue;

import br.com.fiap.hacka.uploadservice.core.commons.FilePart;

public interface MessageProducer {
    void send(String queueName, FilePart filePart);
}