package ru.manager.ProgectManager.DTO.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@Validated
@Schema(description = "Запрос на создание новой записи о произведённой работе")
public class CreateWorkTrackRequest {
    @Schema(description = "Идентификатор задачи, к которой относится работа", required = true)
    private long taskId;
    @Min(value = 1, message = "COUNT_MUST_BE_MORE_1")
    @Schema(description = "Количество отработанных часов", required = true)
    private int workTime;
    @NotBlank(message = "TEXT_MUST_BE_CONTAINS_VISIBLE_SYMBOL")
    @Schema(description = "Описание выполненной работы")
    private String comment;
}
