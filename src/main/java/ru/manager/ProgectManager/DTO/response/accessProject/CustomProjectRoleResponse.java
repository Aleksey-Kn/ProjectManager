package ru.manager.ProgectManager.DTO.response.accessProject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.accessProject.CustomProjectRole;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Информация о кастомной роли")
public class CustomProjectRoleResponse {
    @Schema(description = "Идентификатор роли")
    private final long id;
    @Schema(description = "Название роли")
    private final String name;
    @Schema(description = "Имеет ли право участник с данной ролью создавать и удалять ресурсы проекта")
    private final boolean canEditResource;
    @Schema(description = "Список доступных для данной роли канбан-досок с указанием уровня доступа")
    private final List<CustomRoleWithKanbanConnectorResponse> kanbanConnectorResponses;
    @Schema(description = "Список доступных для данной роли страниц документов с указанием уровня доступа")
    private final List<CustomRoleWithPageConnectorResponse> pageConnectorResponses;

    public CustomProjectRoleResponse(CustomProjectRole role){
        id = role.getId();
        name = role.getName();
        canEditResource = role.isCanEditResources();
        kanbanConnectorResponses = role.getCustomRoleWithKanbanConnectors().stream()
                .map(CustomRoleWithKanbanConnectorResponse::new)
                .collect(Collectors.toList());
        pageConnectorResponses = role.getCustomRoleWithDocumentConnectors().stream()
                .map(CustomRoleWithPageConnectorResponse::new)
                .collect(Collectors.toList());
    }
}
