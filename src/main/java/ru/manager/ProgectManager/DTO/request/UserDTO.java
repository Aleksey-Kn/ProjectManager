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
    @NotBlank(message = "2000") // Login: name of user must contains visible symbols
    @Schema(description = "Логин пользователя")
    private String login;
    @NotBlank(message = "2001") // Password: must contains visible symbols
    @Size(min = 3, message = "Password: must be longer 3 symbols")
    @Schema(description = "Пароль пользователя")
    private String password;
    @Email(message = "2002") // Email: incorrect format
    @Schema(description = "Электронная почта пользователя")
    private String email;
    @NotBlank(message = "2003") // Nickname: must contains visible symbols
    @Schema(description = "Отображаемое имя пользователя")
    private String nickname;
}
