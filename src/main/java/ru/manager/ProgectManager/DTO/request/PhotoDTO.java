package ru.manager.ProgectManager.DTO.request;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class PhotoDTO {
    private MultipartFile file;
}
