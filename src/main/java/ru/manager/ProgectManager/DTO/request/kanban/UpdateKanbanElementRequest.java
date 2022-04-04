package ru.manager.ProgectManager.DTO.request.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Validated
@Schema(description = "Запрос на изменение данных элемента канбана")
public class UpdateKanbanElementRequest {
    @Schema(description = "Текстовое содержимое элемента")
    private String content;
    @NotBlank(message = "NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Schema(description = "Название элемента")
    private String name;
    @Schema(description = "Тег элемента")
    private String tag;
}
