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
    @NotBlank(message = "Password: must contains visible symbols")
    @Size(min = 3, message = "Password: must be longer 3 symbols")
    @Schema(description = "Новый пароль")
    private String newPassword;
    @Email(message = "Email: incorrect format")
    private String email;
    @NotBlank(message = "Nickname: must contains visible symbols")
    private String nickname;
}
