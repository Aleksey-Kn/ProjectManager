package ru.manager.ProgectManager.DTO.request;

import lombok.Data;
import org.springframework.validation.annotation.Validated;
import ru.manager.ProgectManager.enums.TypeRoleProject;

import javax.validation.constraints.NotNull;

@Data
@Validated
public class EditUserRoleRequest {
    private long projectId;
    @NotNull
    private TypeRoleProject typeRoleProject;
    private long customRoleId;
    private long userId;
}
