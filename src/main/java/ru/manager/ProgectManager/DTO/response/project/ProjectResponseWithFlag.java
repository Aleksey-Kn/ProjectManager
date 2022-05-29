package ru.manager.ProgectManager.DTO.response.project;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.Project;

@Getter
@Schema(description = "Сущность проекта c показателем возможности создания и удаления ресурсов текущим пользователем")
public class ProjectResponseWithFlag {
    @Schema(description = "Возможность создавать и удалять ресурсы в данном проекте")
    private final boolean canCreateOrDelete;
    @Schema(description = "Название проекта")
    private final String name;
    @Schema(description = "Идентификатор проекта")
    private final long id;
    @Schema(description = "Фотография профиля проекта", nullable = true)
    private final String photo;
    @Schema(description = "Описание проекта")
    private final String description;
    @Schema(description = "Статус проекта")
    private final String status;
    @Schema(description = "Цвет статуса проекта")
    private final String statusColor;
    @Schema(description = "Дата начала проекта")
    private final String startDate;
    @Schema(description = "Дедлайн проекта")
    private final String deadline;
    @Schema(description = "Название роли этого пользователя в проекте")
    private final String roleName;

    public ProjectResponseWithFlag(Project project, String userRoleName, boolean canRedact) {
        name = project.getName();
        id = project.getId();
        photo = (project.getPhoto() == null? null: "https://api.veehark.xyz/photo/project?id=" + project.getId());
        description = project.getDescription();
        status = project.getStatus();
        statusColor = project.getStatusColor();
        startDate = project.getStartDate();
        deadline = project.getDeadline();
        roleName = userRoleName;
        canCreateOrDelete = canRedact;
    }
}
