package ru.manager.ProgectManager.DTO.request.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Validated
@Schema(description = "Запрос на создание колонки канбана")
public class KanbanColumnRequest {
    @Schema(description = "Идентификатор канбана, в котором будет создана колонка", required = true)
    private long kanbanId;
    @NotBlank(message = "NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Schema(description = "Имя колонки")
    private String name;
}
