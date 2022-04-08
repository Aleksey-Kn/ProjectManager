package ru.manager.ProgectManager.DTO.response.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.kanban.KanbanColumn;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Информация о канбане и его содержимом")
public class KanbanContentResponse {
    @Schema(description = "Название канбана")
    private final String name;
    @Schema(description = "Идентификатор канбана")
    private final long id;
    @Schema(description = "Может ли пользовватель изменять информацию в данной доске")
    private final boolean canEdit;
    @Schema(description = "Список колонок канбана")
    private final List<KanbanColumnResponse> kanbanColumns;

    public KanbanContentResponse(Kanban kanban, int pageIndexColumn, int countColumn, int pageIndexElement,
                                 int countElement, boolean canEditKanban){
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
