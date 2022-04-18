package ru.manager.ProgectManager.DTO.request.accessProject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Уровень доступа к канбану")
public class CustomRoleWithResourceConnectorRequest {
    @Schema(description = "Идентификатор ресурса, к которому нужно предоставить указанный уровень доступа", required = true)
    private long id;
    @Schema(description = "Может ли пользователь редактировать данный ресурс", required = true)
    private boolean canEdit;
}
