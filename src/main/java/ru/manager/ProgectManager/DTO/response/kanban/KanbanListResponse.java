package ru.manager.ProgectManager.DTO.response.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.kanban.Kanban;

import java.util.Set;

@Getter
@AllArgsConstructor
@Schema(description = "Список всех доступных пользователю канбан-досок текущего проекта")
public class KanbanListResponse {
    @Schema(description = "Список канбан-досок")
    private Set<Kanban> kanbans;
}
