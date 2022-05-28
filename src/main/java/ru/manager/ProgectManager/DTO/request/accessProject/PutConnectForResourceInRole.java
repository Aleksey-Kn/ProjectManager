package ru.manager.ProgectManager.DTO.request.accessProject;

import lombok.Data;

import java.util.List;

@Data
public class PutConnectForResourceInRole {
    private long roleId;
    private List<CustomRoleWithResourceConnectorRequest> resourceConnector;
}
