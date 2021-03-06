package ru.manager.ProgectManager.DTO.request.accessProject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;
import ru.manager.ProgectManager.enums.TypeRoleProject;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Validated
@Schema(description = "Запрос на предоставление доступа к проекту")
public class AccessProjectRequest {
    @Schema(description = "Идентификатор проекта, к которому будет предоставлен доступ", required = true)
    private long projectId;
    @NotNull(message = "FIELD_MUST_BE_NOT_NULL")
    @Schema(description = "Тип роли приглашённого пользователя")
    private TypeRoleProject typeRoleProject;
    @Schema(description =
            "Название кастомной роли. Необходимый параметр только в случае, если typeRoleProject выбран CUSTOM_ROLE")
    private long roleId;
    @Schema(description = "Булевая величина, отображающая, будет ли сгенерированная ссылка одноразовой", required = true)
    private boolean disposable;
    @Min(value = 1, message = "COUNT_MUST_BE_MORE_1")
    @Schema(description = "Срок действия ссылки в днях")
    private int liveTimeInDays;
}
