package ru.manager.ProgectManager.DTO.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Validated
@Schema(description = "Данные для смены пароля через письмо на почту")
public class ResetPassRequest {
    @Schema(description = "Токен смены пароля, находящийся в ссылке в отправленном письме", required = true)
    private String token;
    @NotBlank(message = "PASSWORD_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Size(min = 3, message = "PASSWORD_MUST_BE_LONGER_3_SYMBOLS")
    @Schema(description = "Новый пароль")
    private String password;
}
