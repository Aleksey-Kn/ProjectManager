package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Validated
@Getter
@Schema(description = "Запрос на обновление информации о пользователе")
public class RefreshUserDTO {
    @Schema(required = true, description = "Старый пароль")
    private String oldPassword;
    @NotBlank(message = "PASSWORD_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    @Size(min = 3, message = "PASSWORD_MUST_BE_LONGER_3_SYMBOLS")
    @Schema(description = "Новый пароль")
    private String newPassword;
    @Schema(description = "Новые данные об электронной почте пользователя")
    @Email(message = "EMAIL_HAVE_INCORRECT_FORMAT")
    private String email;
    @Schema(description = "Новое отображаемое имя пользователя")
    @NotBlank(message = "NICKNAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS")
    private String nickname;
}
