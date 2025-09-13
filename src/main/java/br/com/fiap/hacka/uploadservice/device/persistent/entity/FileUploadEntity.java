package br.com.fiap.hacka.uploadservice.device.persistent.entity;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "FileUpload")
public class FileUploadEntity {
    @Id
    private String id;

    @Indexed(unique = true)
    private String fileName;

    private String filePath;

    private Date createdAt;
}