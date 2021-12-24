package ru.manager.ProgectManager.DTO.request;

import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Validated
@Getter
public class RefreshUserDTO {
    @NotBlank(message = "Password must contains visible symbols")
    @Size(min = 3, message = "Password must be longer 3 symbols")
    private String password;
    @Email
    private String email;
    @NotBlank
    private String nickname;
}
