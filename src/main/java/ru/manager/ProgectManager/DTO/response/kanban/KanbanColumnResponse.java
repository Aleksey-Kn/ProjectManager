package ru.manager.ProgectManager.DTO.response.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.kanban.KanbanColumn;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.enums.ElementStatus;

import java.util.Comparator;
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

    public KanbanColumnResponse(KanbanColumn kanbanColumn, int pageIndex, int count, int zoneId){
        id = kanbanColumn.getId();
        serialNumber = kanbanColumn.getSerialNumber();
        name = kanbanColumn.getName();
        kanbanElements = (kanbanColumn.getElements() == null
                ? List.of()
                : kanbanColumn.getElements().stream()
                .filter(e -> e.getStatus() == ElementStatus.ALIVE)
                .sorted(Comparator.comparing(KanbanElement::getSerialNumber))
                .map(element -> new KanbanElementMainDataResponse(element, zoneId))
                .skip(pageIndex)
                .limit(count)
                .collect(Collectors.toList()));
    }
}
