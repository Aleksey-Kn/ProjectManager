package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.Project;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ProjectListResponse {
    private final List<ProjectResponse> projects;

    public ProjectListResponse(List<Project> projectList){
        projects = projectList.parallelStream().map(ProjectResponse::new).collect(Collectors.toList());
    }
}
