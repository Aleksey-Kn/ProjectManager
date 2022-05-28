package ru.manager.ProgectManager.DTO.request.accessProject;

import lombok.Data;

@Data
public class DeleteConnectForResourceFromRole {
    private long roleId;
    private long resourceId;
}
