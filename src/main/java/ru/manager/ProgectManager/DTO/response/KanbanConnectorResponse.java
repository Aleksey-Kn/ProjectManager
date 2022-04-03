package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.Kanban;

@Getter
public class KanbanConnectorResponse {
    private final boolean canEdit;
    private final Kanban kanban;

    public KanbanConnectorResponse(CustomRoleWithKanbanConnector connector){
        canEdit = connector.isCanEdit();
        kanban = connector.getKanban();
    }
}
