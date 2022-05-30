package ru.manager.ProgectManager.DTO.response.accessProject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.DTO.response.documents.PageNameResponse;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithDocumentConnector;

@Getter
@Schema(description = "Информация об уровне доступа к странице документа")
public class CustomRoleWithPageConnectorResponse {
    @Schema(description = "Может ли пользователь редактировать данный канбан")
    private final boolean canEdit;
    @Schema(description = "Информация о канбан-доске, к которой имеется доступ")
    private final PageNameResponse page;

    public CustomRoleWithPageConnectorResponse(CustomRoleWithDocumentConnector connector) {
        canEdit = connector.isCanEdit();
        page = new PageNameResponse(connector.getPage(), canEdit);
    }
}
