package ru.manager.ProgectManager.DTO.response;

import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.entitys.UserWithProjectConnector;

import java.util.List;
import java.util.stream.Collectors;

public class AllUserDataResponse {
    private final String username;
    private final String email;
    private final String nickname;
    private final List<Project> userProjects;

    public AllUserDataResponse(User user){
        username = user.getUsername();
        email = user.getEmail();
        nickname = user.getNickname();
        userProjects = user.getUserWithProjectConnectors().stream()
                .map(UserWithProjectConnector::getProject)
                .collect(Collectors.toList());
    }
}
