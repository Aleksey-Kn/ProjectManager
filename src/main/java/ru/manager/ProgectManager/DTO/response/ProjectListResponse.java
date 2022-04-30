package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.Project;

import java.util.LinkedList;
import java.util.List;

@Getter
public class ProjectListResponse {
    private final List<ProjectResponse> projects;

    public ProjectListResponse(List<Project> projectList, List<String> roles, int zoneId){
        projects = new LinkedList<>();
        for(int i = 0; i < projectList.size(); i++){
            projects.add(new ProjectResponse(projectList.get(i), roles.get(i), zoneId));
        }
    }
}
