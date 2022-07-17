package ru.manager.ProgectManager.DTO.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.user.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Schema(description = "Информация об участнике проекта")
@EqualsAndHashCode
public class UserDataWithProjectRoleResponse {
    @Schema(description = "Идентификатор пользователя")
    private final long id;
    @Schema(description = "Отображаемое имя пользователя")
    private final String nickname;
    @Schema(description = "Фотография профиля пользователя", nullable = true)
    private final String photo;
    @Schema(description = "Дата последнего посещения или null в случае регистрации, но отстутствии авторизации",
            nullable = true)
    private final String lastVisit;
    @Schema(description = "Роль в данном проекте")
    private final String role;

    public UserDataWithProjectRoleResponse(User user, String projectRole, int zoneId){
        nickname = user.getNickname();
        id = user.getUserId();
        photo = (user.getPhoto() == null? null: "https://api.veehark.xyz/photo/user?id=" + user.getUserId());
        lastVisit = (user.getLastVisit() == 0? null: LocalDateTime
                .ofEpochSecond(user.getLastVisit(), 0, ZoneOffset.ofHours(zoneId)).toString());
        role = projectRole;
    }
}
