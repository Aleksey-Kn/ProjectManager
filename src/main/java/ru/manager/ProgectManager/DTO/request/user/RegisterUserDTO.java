package ru.manager.ProgectManager.DTO.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;
import ru.manager.ProgectManager.enums.Locale;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Validated
@Schema(description = "Запрос на создание пользователя")
public class RegisterUserDTO {
    @NotBlank(message = "LOGIN_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Schema(description = "Логин пользователя")
    private String login;
    @NotBlank(message = "PASSWORD_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Size(min = 3, message = "PASSWORD_MUST_BE_LONGER_3_SYMBOLS")
    @Schema(description = "Пароль пользователя")
    private String password;
    @Email(message = "EMAIL_HAVE_INCORRECT_FORMAT")
    @Schema(description = "Электронная почта пользователя", required = true)
    private String email;
    @NotBlank(message = "NICKNAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Schema(description = "Отображаемое имя пользователя")
    private String nickname;
    @Schema(description = "Часть url для формирования ссылки на подтверждение почты", required = true)
    private String url;
    @NotNull(message = "FIELD_MUST_BE_NOT_NULL")
    @Schema(description = "Язык письма, отправляемого на почту пользователю")
    private Locale locale;
}
