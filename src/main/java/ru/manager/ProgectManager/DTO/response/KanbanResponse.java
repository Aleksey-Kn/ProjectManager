package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.Kanban;
import ru.manager.ProgectManager.entitys.KanbanColumn;
import ru.manager.ProgectManager.entitys.Project;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Возвращаемая информация о канбане")
public class KanbanResponse {
    @Schema(description = "Список колонок канбана")
    private final List<KanbanColumnResponse> kanbanColumns;
    private final String name;
    private final long id;
    private final Project project;

    public KanbanResponse(Kanban kanban, int pageIndexColumn, int countColumn, int pageIndexElement, int countElement){
        kanbanColumns = kanban.getKanbanColumns().stream()
                .map(kanbanColumn -> new KanbanColumnResponse(kanbanColumn, pageIndexElement, countElement))
                .skip(pageIndexColumn)
                .limit(countColumn)
                .collect(Collectors.toList());
        name = kanban.getName();
        id = kanban.getId();
        project = kanban.getProject();
    }
}
