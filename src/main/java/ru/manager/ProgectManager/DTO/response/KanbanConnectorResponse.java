package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.Kanban;
import ru.manager.ProgectManager.entitys.KanbanConnector;

@Getter
public class KanbanConnectorResponse {
    private final boolean canEdit;
    private final Kanban kanban;

    public KanbanConnectorResponse(KanbanConnector connector){
        canEdit = connector.isCanEdit();
        kanban = connector.getKanban();
    }
}
