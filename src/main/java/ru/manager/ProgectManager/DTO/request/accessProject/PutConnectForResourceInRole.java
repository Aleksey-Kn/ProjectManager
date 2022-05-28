package ru.manager.ProgectManager.DTO.request.accessProject;

import lombok.Data;

@Data
public class PutConnectForResourceInRole {
    private long roleId;
    private CustomRoleWithResourceConnectorRequest resourceConnector;
}
