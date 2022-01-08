package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.UserWithProjectConnector;

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

    public ProjectResponse(Project project){
        name = project.getName();
        id = project.getId();
        photo = project.getPhoto();
        participants = project.getConnectors().stream()
                .map(UserWithProjectConnector::getUser)
                .map(PublicUserDataResponse::new)
                .collect(Collectors.toList());
    }
}
