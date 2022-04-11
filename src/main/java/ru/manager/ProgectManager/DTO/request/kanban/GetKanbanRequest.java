package ru.manager.ProgectManager.DTO.request.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

@Getter
@Validated
@Schema(description = "Запрос на получение канбана с пагинированием по столбцам и их элементам")
public class GetKanbanRequest {
    @Schema(description = "Идентификатор получаемого канбана")
    private long id;
    @Min(value = 0, message = "INDEX_MUST_BE_MORE_0")
    @Schema(description = "Первый порядковый номер колонки в запрашиваемой странице")
    private int pageIndex;
    @Min(value = 1, message = "COUNT_MUST_BE_MORE_1")
    @Schema(description = "Количество колонок в запрашиваемой странице")
    private int count;
}
