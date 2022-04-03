package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@Validated
@Schema(description = "Запрос на изменение или создание кастомной роли")
public class CreateCustomRoleRequest {
    @Schema(description = "Идентификатор проекта, к которому будет относиться роль", required = true)
    private long projectId;
    @NotBlank
    @Schema(description = "Название роли")
    private String name;
    @Schema(description = "Сможет ли пользователь с данной ролью создавать или удалять ресурсы проекта", required = true)
    private boolean canEditResource;
    @Schema(description = "Список доступных для данной роли канбан-досок с указанием уровня доступа", required = true)
    private List<CustomRoleWithKanbanConnectorRequest> customRoleWithKanbanConnectorRequests;
}
