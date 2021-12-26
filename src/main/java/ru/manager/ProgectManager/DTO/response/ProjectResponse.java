package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.UserWithProjectConnector;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ProjectResponse {
    private final String name;
    private final long id;
    private final byte[] photo;
    private final List<AllUserDataResponse> participants;

    public ProjectResponse(Project project){
        name = project.getName();
        id = project.getId();
        photo = project.getPhoto();
        participants = project.getConnectors().stream()
                .map(UserWithProjectConnector::getUser)
                .map(AllUserDataResponse::new)
                .collect(Collectors.toList());
    }
}
