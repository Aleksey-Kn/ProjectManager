package ru.manager.ProgectManager.DTO.request.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Validated
@Schema(description = "Запрос на создание элемента канбана")
public class CreateKanbanElementRequest {
    @Schema(description = "Идентификатор колонки, в которой будет создан элемент", required = true)
    private long columnId;
    @Schema(description = "Текст внутри элемента")
    private String content;
    @NotBlank(message = "NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Schema(description = "Имя элемента")
    private String name;
    @Schema(description = "Тег элемента")
    private String tag;
}
