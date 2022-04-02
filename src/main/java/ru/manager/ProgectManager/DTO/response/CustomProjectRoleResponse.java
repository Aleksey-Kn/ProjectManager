package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.CustomProjectRole;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CustomProjectRoleResponse {
    private final String name;
    private final boolean canEditResource;
    private final List<KanbanConnectorResponse> kanbanConnectorRequests;

    public CustomProjectRoleResponse(CustomProjectRole role){
        name = role.getName();
        canEditResource = role.isCanEditResources();
        kanbanConnectorRequests = role.getKanbanConnectors().stream().map(KanbanConnectorResponse::new)
                .collect(Collectors.toList());
    }
}
