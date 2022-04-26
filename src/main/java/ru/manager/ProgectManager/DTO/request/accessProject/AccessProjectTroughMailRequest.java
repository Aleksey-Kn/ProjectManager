package ru.manager.ProgectManager.DTO.request.accessProject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import ru.manager.ProgectManager.enums.Locale;
import ru.manager.ProgectManager.enums.TypeRoleProject;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Validated
@Schema(description = "Запрос на отправку реферальной ссылки на почту")
public class AccessProjectTroughMailRequest {
    @Schema(description = "Идентификатор проекта, к которому будет предоставлен доступ", required = true)
    private long projectId;
    @NotNull(message = "FIELD_MUST_BE_NOT_NULL")
    @Schema(description = "Тип роли приглашённого пользователя")
    private TypeRoleProject typeRoleProject;
    @Schema(description =
            "Название кастомной роли. Необходимый параметр только в случае, если typeRoleProject выбран CUSTOM_ROLE")
    private long roleId;
    @Min(value = 1, message = "COUNT_MUST_BE_MORE_1")
    @Schema(description = "Срок действия ссылки в днях")
    private int liveTimeInDays;
    @Email(message = "EMAIL_HAVE_INCORRECT_FORMAT")
    @Schema(description = "Почта, на которую будет отправлена реферальная ссылка", required = true)
    private String email;
    @Schema(description = "Часть url для формирования реферальной ссылки", required = true)
    private String url;
    @NotNull(message = "FIELD_MUST_BE_NOT_NULL")
    @Schema(description = "Язык письма, отправляемого на почту пользователю")
    private Locale locale;
}
