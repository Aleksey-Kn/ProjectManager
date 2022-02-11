package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.KanbanColumn;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Возвращяаемая информация о колонке канбана")
public class KanbanColumnResponse {
    @Schema(description = "Идентификатор колонки")
    private final long id;
    @Schema(description = "Порядковый номер колонки канбана")
    private final int serialNumber;
    @Schema(description = "Порядковый номер колонки в канбане")
    private final String name;
    @Schema(description = "Список элементов, содержащихся в данной колонке")
    private final List<KanbanElementMainDataResponse> kanbanElements;

    public KanbanColumnResponse(KanbanColumn kanbanColumn, int pageIndex, int count){
        id = kanbanColumn.getId();
        serialNumber = kanbanColumn.getSerialNumber();
        name = kanbanColumn.getName();
        kanbanElements = (kanbanColumn.getElements() == null
                ? List.of()
                : kanbanColumn.getElements().stream()
                .map(KanbanElementMainDataResponse::new)
                .skip(pageIndex)
                .limit(count)
                .collect(Collectors.toList()));
    }
}
