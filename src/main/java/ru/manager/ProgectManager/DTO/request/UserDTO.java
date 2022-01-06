package ru.manager.ProgectManager.DTO.request;

import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Validated
public class UserDTO {
    @NotBlank(message = "Login: name of user must contains visible symbols")
    private String login;
    @NotBlank(message = "Password: must contains visible symbols")
    @Size(min = 3, message = "Password: must be longer 3 symbols")
    private String password;
    @Email(message = "Email: incorrect format")
    private String email;
    @NotBlank(message = "Nickname: must contains visible symbols")
    private String nickname;
}
