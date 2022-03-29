package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Validated
@Schema(description = "Форма для отправки данных проекта")
public class ProjectDataRequest {
    @NotBlank
    @Schema(description = "Название проекта")
    private String name;
    @Schema(description = "Описание проекта")
    private String description;
    @Schema(description = "Статус проекта")
    private String status;
    @Schema(description = "Цвет статуса проекта")
    private String statusColor;
    @Schema(description = "Дата начала проекта")
    private String startDate;
    @Schema(description = "Дедлайн проекта")
    private String deadline;
}
