package ru.manager.ProgectManager.DTO.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import ru.manager.ProgectManager.enums.Locale;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
    @NotEmpty(message = "FIELD_MUST_BE_NOT_NULL")
    @Schema(description = "IP адрес, с которого осуществляется авторизация")
    private String ip;
    @NotEmpty(message = "FIELD_MUST_BE_NOT_NULL")
    @Schema(description = "Браузер, из которого осуществляется авторизация")
    private String browser;
    @NotEmpty(message = "FIELD_MUST_BE_NOT_NULL")
    @Schema(description = "Страна, из которой осуществляется авторизация")
    private String country;
    @NotEmpty(message = "FIELD_MUST_BE_NOT_NULL")
    @Schema(description = "Город, из которого осуществляется авторизация")
    private String city;
    @NotNull(message = "FIELD_MUST_BE_NOT_NULL")
    @Schema(description = "Язык письма, отправляемого на почту пользователю")
    private Locale locale;
    @Schema(description = "Часовой пояс текущего пользователя")
    private int zoneId;
}
