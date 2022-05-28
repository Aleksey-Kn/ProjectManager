package ru.manager.ProgectManager.DTO.request.accessProject;

import lombok.Data;

import java.util.List;

@Data
public class DeleteConnectForResourceFromRole {
    private long roleId;
    private List<Long> resourceId;
}
