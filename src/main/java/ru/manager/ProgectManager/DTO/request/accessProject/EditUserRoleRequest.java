package ru.manager.ProgectManager.DTO.request.accessProject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import ru.manager.ProgectManager.enums.TypeRoleProject;

import javax.validation.constraints.NotNull;

@Data
@Validated
@Schema(description = "Запрос на присвоение пользоваателю определённой роли")
public class EditUserRoleRequest {
    @NotNull
    @Schema(description = "Тип присваеваемой роли")
    private TypeRoleProject typeRoleProject;
    @Schema(description = "Идентификатор присваиваемой роли", required = true)
    private long roleId;
    @Schema(description = "Идентификатор пользователя, которому присваивается указанная роль", required = true)
    private long userId;
}
