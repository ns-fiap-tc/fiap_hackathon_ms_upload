package br.com.fiap.hacka.uploadservice.device.service;

import br.com.fiap.hacka.uploadservice.core.commons.FileUploadMapper;
import br.com.fiap.hacka.uploadservice.device.core.domain.FileUpload;
import br.com.fiap.hacka.uploadservice.device.persistence.repository.FileUploadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FileUploadService {
    private final FileUploadRepository repository;
    private FileUploadMapper MAPPER = FileUploadMapper.INSTANCE;

    public void save(FileUpload fileUpload) {
        this.repository.save(MAPPER.toEntity(fileUpload));
    }
}