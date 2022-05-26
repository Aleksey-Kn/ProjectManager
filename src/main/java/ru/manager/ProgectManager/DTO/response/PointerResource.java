package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.documents.Page;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.enums.ResourceType;

public class PointerResource {
    @Getter
    private static class ProjectInfo{
        long id;
        String name;

        ProjectInfo(Project project) {
            id = project.getId();
            name = project.getName();
        }
    }

    @Getter
    private final long id;
    @Getter
    private final String name;
    private final ResourceType resourceType;
    @Getter
    private ProjectInfo project;
    @Getter
    private String description;

    public PointerResource(Page page) {
        id = page.getId();
        name = page.getName();
        resourceType = ResourceType.DOCUMENT;
        project = new ProjectInfo(page.getProject());
    }

    public PointerResource(Kanban kanban) {
        id = kanban.getId();
        name = kanban.getName();
        resourceType = ResourceType.KANBAN;
        project = new ProjectInfo(kanban.getProject());
    }

    public PointerResource(Project project) {
        id = project.getId();
        name = project.getName();
        resourceType = ResourceType.PROJECT;
        description = project.getDescription();
    }

    public String getResourceType() {
        return resourceType.getStringValue();
    }
}
