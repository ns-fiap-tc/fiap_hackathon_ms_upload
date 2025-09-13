package br.com.fiap.hacka.uploadservice.core.commons;

import br.com.fiap.hacka.uploadservice.device.core.domain.FileUpload;
import br.com.fiap.hacka.uploadservice.device.persistent.entity.FileUploadEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FileUploadMapper {
    FileUploadMapper INSTANCE = Mappers.getMapper(FileUploadMapper.class);

    FileUploadEntity toEntity(FileUpload fileUpload);
    FileUpload toDomain(FileUploadEntity entity);
}