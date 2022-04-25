package ru.manager.ProgectManager.DTO.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Validated
@Schema(description = "Данные запроса обновления пароля через текущий пароль")
public class UpdatePassRequest {
    @NotBlank(message = "PASSWORD_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Size(min = 3, message = "PASSWORD_MUST_BE_LONGER_3_SYMBOLS")
    @Schema(description = "Текущий пароль")
    private String oldPass;
    @NotBlank(message = "PASSWORD_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Size(min = 3, message = "PASSWORD_MUST_BE_LONGER_3_SYMBOLS")
    @Schema(description = "Новый пароль")
    private String newPass;
}
