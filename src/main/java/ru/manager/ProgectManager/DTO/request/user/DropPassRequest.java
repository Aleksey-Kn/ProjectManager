package ru.manager.ProgectManager.DTO.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
@Schema(description = "Данные запроса на сброс пароля через письмо на почту")
public class DropPassRequest {
    @NotBlank(message = "LOGIN_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Schema(description = "Логин или почта аккаунта, пароль которого пользователь желает сбросить")
    private String loginOrEmail;
    @Schema(description = "Часть url для формирования ссылки на сброс пароля")
    private String url;
}
