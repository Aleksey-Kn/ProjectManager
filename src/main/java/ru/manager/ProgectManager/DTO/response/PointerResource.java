package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.documents.Page;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.enums.ResourceType;

@Schema(description = "Результат поиска ресрусов по имени")
public class PointerResource {
    @Getter
    @Schema(description = "Идентификатор ресурса")
    private final long id;
    @Getter
    @Schema(description = "Название ресурса")
    private final String name;
    @Schema(description = "Тип ресурса")
    private final ResourceType resourceType;
    @Getter
    @Schema(description = "Данные проекта, если ресурс - не проект", nullable = true)
    private ProjectInfo project;
    @Getter
    @Schema(description = "Описание проекта, если ресурс - проект", nullable = true)
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
