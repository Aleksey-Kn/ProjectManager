package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Schema(description = "Информация об ошибках текущего запроса")
public class ErrorResponse {
    @Schema(description = "Список ошибок", example = "Источник: причина")
    private final List<String> errors;
}
