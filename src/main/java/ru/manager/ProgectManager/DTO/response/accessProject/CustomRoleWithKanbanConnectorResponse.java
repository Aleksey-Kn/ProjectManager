package ru.manager.ProgectManager.DTO.response.accessProject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.kanban.Kanban;

@Getter
@Schema(description = "Уровень доступа к канбану")
public class CustomRoleWithKanbanConnectorResponse {
    @Schema(description = "Может ли пользователь редактировать данный канбан")
    private final boolean canEdit;
    @Schema(description = "Информация о канбан-доске, к которой имеется доступ")
    private final Kanban kanban;

    public CustomRoleWithKanbanConnectorResponse(CustomRoleWithKanbanConnector connector){
        canEdit = connector.isCanEdit();
        kanban = connector.getKanban();
    }
}
