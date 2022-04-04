package ru.manager.ProgectManager.DTO.request.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.enums.SortType;

@Getter
@Schema(description = "Запрос на сортировку коронки канбана")
public class SortColumnRequest {
    @Schema(description = "Идентификатор колонки", required = true)
    private long id;
    @Schema(description = "Параметр сортировки. Может быть TIME_UPDATE, TIME_CREATE или ALPHABET", required = true)
    private SortType type;
    @Schema(description = "Флаг для сортрирования по убыванию", required = true)
    private boolean reverse;
}
