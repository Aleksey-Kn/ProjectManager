package ru.manager.ProgectManager.DTO.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
@Schema(description = "Даннные для авторизации пользователя в системе")
public class AuthDto {
    @NotBlank(message = "LOGIN_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Schema(description = "Логин или почта пользователя")
    private String login;
    @NotBlank(message = "PASSWORD_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Schema(description = "Пароль пользователя")
    private String password;
}
