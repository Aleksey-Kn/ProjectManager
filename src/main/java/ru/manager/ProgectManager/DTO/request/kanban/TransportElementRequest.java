package ru.manager.ProgectManager.DTO.request.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Validated
@Getter
@Schema(description = "Запрос на передвижение элемента канбана")
public class TransportElementRequest {
    @Schema(required = true, description = "Идентификатор передвигаемого элемента")
    private long id;
    @Schema(description = "Новый индекс указанного элемента")
    @Min(value = 0, message = "INDEX_MUST_BE_MORE_0")
    private int toIndex;
    @Min(value = 0, message = "INDEX_MUST_BE_MORE_0")
    @Schema(description = "Идентификатор целевой колонки элемента")
    private int toColumn;
}
