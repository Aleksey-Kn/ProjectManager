package ru.manager.ProgectManager.DTO.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.user.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Schema(description = "Информация о пользователе для администратора сервиса")
public class UserDataForAdmin {
    @Schema(description = "Идентификатор пользователя")
    private final long id;
    @Schema(description = "Адрес электронной почты пользователя")
    private final String email;
    @Schema(description = "Отображаемое имя пользователя")
    private final String nickname;
    @Schema(description = "Фотография профиля пользователя", nullable = true)
    private final String photo;
    @Schema(description = "Дата последнего посещения или null в случае регистрации, но отстутствии авторизации",
            nullable = true)
    private final String lastVisit;
    @Schema(description = "Заблокирован ли данный аккаунт")
    private final boolean nonLocked;
    @Schema(description = "Логин пользователя")
    private final String login;

    public UserDataForAdmin(User user, int zoneId) {
        email = user.getEmail();
        nickname = user.getNickname();
        id = user.getUserId();
        photo = (user.getPhoto() == null? null: "https://api.veehark.xyz/photo/user?id=" + user.getUserId());
        lastVisit = (user.getLastVisit() == 0? null: LocalDateTime
                .ofEpochSecond(user.getLastVisit(), 0, ZoneOffset.ofHours(zoneId)).toString());
        nonLocked = user.isAccountNonLocked();
        login = user.getUsername();
    }
}
