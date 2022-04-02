package ru.manager.ProgectManager.DTO.request;

import lombok.Data;
import ru.manager.ProgectManager.enums.TypeRoleProject;

@Data
public class EditUserRoleRequest {
    private long projectId;
    private TypeRoleProject typeRoleProject;
    private long customRoleId;
    private long userId;
}
