package br.com.fiap.hacka.uploadservice.device.persistence.repository;

import br.com.fiap.hacka.uploadservice.device.persistent.entity.FileUploadEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FileUploadRepository extends MongoRepository<FileUploadEntity, String> {
    @Query("{'fileName' : ?0}")
    FileUploadEntity findByFileName(String fileName);
}