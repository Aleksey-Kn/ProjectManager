package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.entitys.UserWithProjectConnector;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Предоставление полной информации о пользователе")
public class AllUserDataResponse {
    @Schema(description = "Идентифткатор пользователя")
    private final long id;
    @Schema(description = "Электронная почта пользователя")
    private final String email;
    @Schema(description = "Отображаемое имя пользовалеля")
    private final String nickname;
    @Schema(description = "Список проектов, к которым у пользователя есть доступ")
    private final List<Project> userProjects;
    @Schema(description = "Фото профиля пользователя", nullable = true)
    private final byte[] photo;

    public AllUserDataResponse(User user){
        email = user.getEmail();
        nickname = user.getNickname();
        userProjects = user.getUserWithProjectConnectors().stream()
                .map(UserWithProjectConnector::getProject)
                .collect(Collectors.toList());
        id = user.getUserId();
        photo = user.getPhoto();
    }
}
