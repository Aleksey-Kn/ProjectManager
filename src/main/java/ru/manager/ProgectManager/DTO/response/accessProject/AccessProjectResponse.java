package ru.manager.ProgectManager.DTO.response.accessProject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.manager.ProgectManager.entitys.accessProject.AccessProject;

@Data
@Schema(description = "Ответ для предоставления доступа к проекту")
public class AccessProjectResponse {
    @Schema(description = "Токен доступа к проекту")
    private final String refToken;

    public AccessProjectResponse(AccessProject accessProject) {
        refToken = accessProject.getCode();
    }
}
