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
    private final List<KanbanColumn> kanbanColumns;

    public KanbanContentResponse(Kanban kanban, int pageIndex, int count, boolean canEditKanban){
        kanbanColumns = kanban.getKanbanColumns().stream()
                .sorted(Comparator.comparing(KanbanColumn::getSerialNumber))
                .skip(pageIndex)
                .limit(count)
                .collect(Collectors.toList());
        name = kanban.getName();
        id = kanban.getId();
        canEdit = canEditKanban;
    }
}
