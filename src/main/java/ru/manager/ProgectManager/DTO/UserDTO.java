package ru.manager.ProgectManager.DTO;

import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Validated
public class UserDTO {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
