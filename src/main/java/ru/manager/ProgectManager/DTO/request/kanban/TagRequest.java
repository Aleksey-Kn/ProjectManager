package ru.manager.ProgectManager.DTO.request.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@Schema(description = "Запрос на добавление нового тега в канбан")
public class TagRequest {
    @Schema(description = "Текстовая составляющая тега")
    private String text;
    @Schema(description = "Цветовая составляющая тега")
    private String color;
}
