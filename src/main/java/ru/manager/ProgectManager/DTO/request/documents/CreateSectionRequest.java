package ru.manager.ProgectManager.DTO.request.documents;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
public class CreateSectionRequest {
    private long projectId;
    private long parentId;
    @NotBlank
    private String name;
    private String content;
}
