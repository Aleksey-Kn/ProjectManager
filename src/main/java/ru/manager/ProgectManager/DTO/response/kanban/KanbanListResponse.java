package ru.manager.ProgectManager.DTO.response.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.kanban.Kanban;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Список всех доступных пользователю канбан-досок текущего проекта")
public class KanbanListResponse {
    @Schema(description = "Список канбан-досок")
    private final List<KanbanMainDataResponse> kanbans;

    public KanbanListResponse(Set<Kanban> kanbanSet, int zoneId){
        kanbans = kanbanSet.stream()
                .map(kanban -> new KanbanMainDataResponse(kanban, zoneId))
                .collect(Collectors.toList());
    }
}
