package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.Kanban;
import ru.manager.ProgectManager.entitys.KanbanColumn;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Возвращаемая информация о канбане")
public class KanbanResponse {
    @Schema(description = "Название канбана")
    private final String name;
    @Schema(description = "Идентификатор канбана")
    private final long id;
    @Schema(description = "Может ли пользовватель изменять информацию в данной доске")
    private final boolean canEdit;
    @Schema(description = "Список колонок канбана")
    private final List<KanbanColumnResponse> kanbanColumns;

    public KanbanResponse(Kanban kanban, int pageIndexColumn, int countColumn, int pageIndexElement, int countElement,
                          boolean canEditKanban){
        kanbanColumns = kanban.getKanbanColumns().stream()
                .sorted(Comparator.comparing(KanbanColumn::getSerialNumber))
                .map(kanbanColumn -> new KanbanColumnResponse(kanbanColumn, pageIndexElement, countElement))
                .skip(pageIndexColumn)
                .limit(countColumn)
                .collect(Collectors.toList());
        name = kanban.getName();
        id = kanban.getId();
        canEdit = canEditKanban;
    }
}
