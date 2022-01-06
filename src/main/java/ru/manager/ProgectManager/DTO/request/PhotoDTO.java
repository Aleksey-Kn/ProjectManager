package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Schema(description = "Запрос на добавление фотографии")
public class PhotoDTO {
    @Schema(required = true, description = "Добавляемая фотография")
    private MultipartFile file;
}
