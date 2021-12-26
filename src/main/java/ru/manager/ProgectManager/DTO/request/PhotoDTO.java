package ru.manager.ProgectManager.DTO.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PhotoDTO {
    private MultipartFile file;
}
