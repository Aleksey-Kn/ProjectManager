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

    public KanbanListResponse(Set<Kanban> kanbanSet){
        kanbans = kanbanSet.stream().map(KanbanMainDataResponse::new).collect(Collectors.toList());
    }
}
