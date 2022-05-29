package ru.manager.ProgectManager.DTO.request.accessProject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Validated
@Schema(description = "Данные для удаления доступа к ресурсам")
public class DeleteConnectForResourceFromRole {
    @Schema(description = "Идентификаор изменяемой роли", required = true)
    private long roleId;
    @NotNull
    @Schema(description = "Идентификатор ресурсов, к которым следует ограничить доступ")
    private List<Long> resourceId;
}
