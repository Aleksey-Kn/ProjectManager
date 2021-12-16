package ru.manager.ProgectManager.DTO;

import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Validated
public class UserDTO {
    @NotBlank(message = "Имя пользователя должно содержать видимые символы")
    private String username;
    @NotBlank(message = "Пароль должен содержать видимые символы")
    @Size(min = 3, message = "Пароль болжен быть длиннее трёх символов")
    private String password;
}
