package br.com.fiap.hacka.uploadservice.device.core.domain;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class FileUpload {
    private String id;
    private String filePath;
    private String fileName;
    private Date createdAt;
}