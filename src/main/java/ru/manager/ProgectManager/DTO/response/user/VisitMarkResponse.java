package ru.manager.ProgectManager.DTO.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.DTO.response.project.ProjectInfo;
import ru.manager.ProgectManager.entitys.user.VisitMark;
import ru.manager.ProgectManager.enums.ResourceType;

@Getter
@Schema(description = "Информация о посещённом ресурсе")
public class VisitMarkResponse {
    @Schema(description = "Идентификатор посещённого ресурса")
    private final long id;
    @Schema(description = "Тип посещённого ресурса")
    private final String type;
    @Schema(description = "Название посещённого ресурса")
    private final String name;
    @Schema(description = "Описание проекта, если ресурс - проект", nullable = true)
    private final String description;
    @Schema(description = "Данные проекта, если ресурс - не проект", nullable = true)
    private final ProjectInfo project;

    public VisitMarkResponse(VisitMark visitMark) {
        id = visitMark.getResourceId();
        name = visitMark.getResourceName();
        type = visitMark.getResourceType().getStringValue();
        if(visitMark.getResourceType() != ResourceType.PROJECT) {
            description = null;
            project = new ProjectInfo(visitMark.getProjectId(), visitMark.getProjectName());
        } else {
            description = visitMark.getDescription();
            project = null;
        }
    }
}
