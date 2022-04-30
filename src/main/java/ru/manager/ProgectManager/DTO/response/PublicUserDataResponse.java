package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.user.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Schema(description = "Информация о пользователе, находящаяся в публичном доступе")
public class PublicUserDataResponse {
    @Schema(description = "Идентификатор пользователя")
    private final long id;
    @Schema(description = "Адрес электронной почты пользователя")
    private final String email;
    @Schema(description = "Отображаемое имя пользователя")
    private final String nickname;
    @Schema(description = "Фотография профиля пользователя", nullable = true)
    private final byte[] photo;
    @Schema(description = "Тип данных фотографии")
    private final String datatypePhoto;
    @Schema(description = "Дата последнего посещения или null в случае регистрации, но отстутствии авторизации",
            nullable = true)
    private final String lastVisit;

    public PublicUserDataResponse(User user){
        email = user.getEmail();
        nickname = user.getNickname();
        id = user.getUserId();
        photo = user.getPhoto();
        datatypePhoto = user.getContentTypePhoto();
        lastVisit = (user.getLastVisit() == 0? null: LocalDateTime
                .ofEpochSecond(user.getLastVisit(), 0, ZoneOffset.ofHours(user.getZoneId())).toString());
    }
}
