package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.manager.ProgectManager.entitys.Project;

@Getter
@RequiredArgsConstructor
@Schema(description = "Краткая информация о проекте")
public class ProjectInfo{
    @Schema(description = "Идентификатор проекта")
    private final long id;
    @Schema(description = "Название проекта")
    private final String name;

    ProjectInfo(Project project) {
        id = project.getId();
        name = project.getName();
    }
}
