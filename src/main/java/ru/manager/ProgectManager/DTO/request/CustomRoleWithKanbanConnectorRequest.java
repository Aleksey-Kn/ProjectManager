package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Уровень доступа к канбану")
public class CustomRoleWithKanbanConnectorRequest {
    @Schema(description = "Идентификатор канбана, к которому нужно предоставить указанный уровень доступа", required = true)
    private long id;
    @Schema(description = "Может ли пользователь редактировать данный канбан", required = true)
    private boolean canEdit;
}
