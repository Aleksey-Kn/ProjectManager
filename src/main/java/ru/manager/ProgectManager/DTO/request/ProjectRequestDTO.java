package ru.manager.ProgectManager.DTO.request;

import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
@Getter
public class ProjectRequestDTO {
    @NotBlank(message = "Name must contains visible symbols")
    private String name;
}
