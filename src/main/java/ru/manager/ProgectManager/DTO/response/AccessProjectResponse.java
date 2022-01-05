package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.AccessProject;

@Getter
public class AccessProjectResponse {
    private String token;
    private String projectName;

    public AccessProjectResponse(AccessProject accessProject){
        projectName = accessProject.getProject().getName();
        token = accessProject.getCode();
    }
}
