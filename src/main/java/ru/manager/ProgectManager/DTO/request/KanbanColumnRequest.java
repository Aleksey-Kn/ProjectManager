package ru.manager.ProgectManager.DTO.request;

import lombok.Getter;

@Getter
public class KanbanColumnRequest {
    private long projectId;
    private String name;
}
