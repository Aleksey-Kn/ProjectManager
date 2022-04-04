package ru.manager.ProgectManager.DTO.request.kanban;

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
    @Min(value = 0, message = "INDEX_MUST_BE_MORE_0")
    @Schema(description = "Новый индекс указанной колонки")
    private int to;
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
