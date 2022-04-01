package ru.manager.ProgectManager.DTO.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateCustomRoleRequest {
    private String name;
    private boolean canEditResource;
    private List<KanbanConnectorRequest> kanbanConnectorRequests;
}
