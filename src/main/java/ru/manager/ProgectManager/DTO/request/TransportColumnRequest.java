package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Getter
@Validated
@Schema(description = "Запрос на передвижение колонки канбана")
public class TransportColumnRequest {
    @Schema(required = true, description = "Идентификатор передвигаемой колонки")
    private long id;
    @Min(value = 0, message = "Index must be more 0")
    @Schema(description = "Новый индекс указанной колонки")
    private int to;
}
