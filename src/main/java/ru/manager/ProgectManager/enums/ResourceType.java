package ru.manager.ProgectManager.enums;

import lombok.Getter;

@Getter
public enum ResourceType {
    KANBAN("project-kanban-id"), DOCUMENT("project-doc-page-id"), PROJECT("project");

    private final String stringValue;

    ResourceType(String value){
        stringValue = value;
    }
}
