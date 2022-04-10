package ru.manager.ProgectManager.DTO.request.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
@Schema(description = "Запрос на создание нового чекбокса в элементе канбан-доски")
public class CheckboxRequest {
    @Schema(description = "Идентификатор элемента, в который будет помещён чекбокс", required = true)
    private long elementId;
    @NotBlank
    @Schema(description = "Текстовая часть чекбокса")
    private String text;
}
