package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Validated
@Schema(description = "Запрос на создание пользователя")
public class UserDTO {
    @NotBlank(message = "LOGIN_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Schema(description = "Логин пользователя")
    private String login;
    @NotBlank(message = "PASSWORD_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Size(min = 3, message = "PASSWORD_MUST_BE_LONGER_3_SYMBOLS")
    @Schema(description = "Пароль пользователя")
    private String password;
    @Email(message = "EMAIL_HAVE_INCORRECT_FORMAT")
    @Schema(description = "Электронная почта пользователя")
    private String email;
    @NotBlank(message = "NICKNAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Schema(description = "Отображаемое имя пользователя")
    private String nickname;
}
