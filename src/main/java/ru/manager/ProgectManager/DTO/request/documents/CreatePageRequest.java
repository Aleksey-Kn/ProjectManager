package ru.manager.ProgectManager.DTO.request.documents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
@Schema(description = "Данные для создания новой страницы документа")
public class CreatePageRequest {
    @Schema(description = "Идентификатор проекта, в котором требуется создать страницу", required = true)
    private long projectId;
    @Schema(description = "Идентификатор родительской страницы")
    private long parentId;
    @NotBlank
    @Schema(description = "Название страницы")
    private String name;
    @Schema(description = "Данные документа")
    private String content;
}
