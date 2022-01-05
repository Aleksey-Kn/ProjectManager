package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Validated
@Schema(description = "Запрос на создание колонки канбана")
public class KanbanColumnRequest {
    @Schema(description = "Идентификатор проекта, в котором будет создана колонка", required = true)
    private long projectId;
    @NotBlank
    @Schema(description = "Имя колонки")
    private String name;
}
