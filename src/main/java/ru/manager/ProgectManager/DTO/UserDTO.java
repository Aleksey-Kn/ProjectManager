package ru.manager.ProgectManager.DTO;

import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Validated
public class UserDTO {
    @NotBlank(message = "Name of user must contains visible symbols")
    private String login;
    @NotBlank(message = "Password must contains visible symbols")
    @Size(min = 3, message = "Password must be longer 3 symbols")
    private String password;
}
