package ru.manager.ProgectManager.DTO.request;

import lombok.Data;

@Data
public class KanbanConnectorRequest {
    private long id;
    private boolean canEdit;
}
