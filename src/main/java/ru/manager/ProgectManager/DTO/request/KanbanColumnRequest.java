package ru.manager.ProgectManager.DTO.request;

import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Validated
public class KanbanColumnRequest {
    private long projectId;
    @NotBlank
    private String name;
}
