package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.AccessProject;

@Getter
@Schema(description = "Ответ для предоставления доступа к проекту")
public class AccessProjectResponse {
    @Schema(description = "Токен доступа к проекту")
    private final String token;
    @Schema(description = "Название проекта, к которому предоставляется доступ")
    private final String projectName;

    public AccessProjectResponse(AccessProject accessProject){
        projectName = accessProject.getProject().getName();
        token = accessProject.getCode();
    }
}
