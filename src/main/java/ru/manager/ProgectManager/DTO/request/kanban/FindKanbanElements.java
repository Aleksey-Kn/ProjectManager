package ru.manager.ProgectManager.DTO.request.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;
import ru.manager.ProgectManager.enums.ElementStatus;
import ru.manager.ProgectManager.enums.SearchElementType;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Validated
@Schema(description = "Данные для поиска элементов канбана по имени")
public class FindKanbanElements {
    @Schema(description = "Идентификатор канбан-доски, в которой выполняется поиск элементов", required = true)
    private long kanbanId;
    @NotNull(message = "FIELD_MUST_BE_NOT_NULL")
    @Schema(description = "Поле элемента, по которому будет производиться поиск")
    private SearchElementType type;
    @NotNull(message = "FIELD_MUST_BE_NOT_NULL")
    @Schema(description = "Раздел канбана, в котором будет производится поиск")
    private ElementStatus status;
    @NotBlank(message = "NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Schema(description = "Искомое название")
    private String name;
    @Min(value = 0, message = "INDEX_MUST_BE_MORE_0")
    @Schema(description = "Первый порядковый номер в запрашиваемой странице")
    private int pageIndex;
    @Min(value = 1, message = "COUNT_MUST_BE_MORE_1")
    @Schema(description = "Количество элементов в запрашиваемой странице")
    private int count;
}
