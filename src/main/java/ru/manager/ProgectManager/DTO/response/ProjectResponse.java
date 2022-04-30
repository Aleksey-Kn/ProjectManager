package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Возвращаемая информация о проекте")
public class ProjectResponse {
    @Schema(description = "Название проекта")
    private final String name;
    @Schema(description = "Идентификатор проекта")
    private final long id;
    @Schema(description = "Фотография профиля проекта", nullable = true)
    private final byte[] photo;
    @Schema(description = "Список участников проекта")
    private final List<PublicUserDataResponse> participants;
    @Schema(description = "Тип данных фотографии")
    private final String datatypePhoto;
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

    public ProjectResponse(Project project, String userRoleName, int zoneId){
        name = project.getName();
        id = project.getId();
        photo = project.getPhoto();
        participants = project.getConnectors().stream()
                .map(UserWithProjectConnector::getUser)
                .map(user -> new PublicUserDataResponse(user, zoneId))
                .limit(3)
                .collect(Collectors.toList());
        datatypePhoto = project.getDatatypePhoto();
        description = project.getDescription();
        status = project.getStatus();
        statusColor = project.getStatusColor();
        startDate = project.getStartDate();
        deadline = project.getDeadline();
        roleName = userRoleName;
    }
}
