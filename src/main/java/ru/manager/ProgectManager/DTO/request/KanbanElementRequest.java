package ru.manager.ProgectManager.DTO.request;

import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Validated
public class KanbanElementRequest {
    private String content;
    @NotBlank(message = "Name must be contains visible symbols")
    private String name;
    private String tag;
}
