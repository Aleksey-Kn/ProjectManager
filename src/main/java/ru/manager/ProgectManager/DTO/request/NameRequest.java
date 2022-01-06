package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
@Getter
@Schema(description = "Запрос на изменение названия")
public class NameRequest {
    @NotBlank(message = "Name: must contains visible symbols")
    @Schema(description = "Устанаваливаемое название")
    private String name;
}
