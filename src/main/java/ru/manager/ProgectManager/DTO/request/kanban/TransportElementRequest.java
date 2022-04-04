package ru.manager.ProgectManager.DTO.request.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Validated
@Getter
@Schema(description = "Запрос на передвижение елемента канбана")
public class TransportElementRequest {
    @Schema(required = true, description = "Идентификатор передвигаемого элемента")
    private long id;
    @Schema(description = "Новый индекс указанного элемента")
    @Min(value = 0, message = "INDEX_MUST_BE_MORE_0")
    private int toIndex;
    @Min(value = 0, message = "INDEX_MUST_BE_MORE_0")
    @Schema(description = "Индентификатор целевой колонки элемента")
    private int toColumn;
    @Min(value = 0, message = "INDEX_MUST_BE_MORE_0")
    @Schema(description = "Первый порядковый номер колонки в запрашиваемой странице")
    private int pageColumnIndex;
    @Min(value = 1, message = "COUNT_MUST_BE_MORE_1")
    @Schema(description = "Количество колонок в запрашиваемой странице")
    private int countColumn;
    @Min(value = 0, message = "INDEX_MUST_BE_MORE_0")
    @Schema(description = "Первый порядковый номер элемента в запрашиваемой странице")
    private int pageElementIndex;
    @Min(value = 1, message = "COUNT_MUST_BE_MORE_1")
    @Schema(description = "Количество элементов в запрашиваемой странице")
    private int countElement;
}
