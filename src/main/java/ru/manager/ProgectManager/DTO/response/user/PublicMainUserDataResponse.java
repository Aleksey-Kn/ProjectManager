package ru.manager.ProgectManager.DTO.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.tomcat.util.codec.binary.Base64;
import ru.manager.ProgectManager.entitys.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Schema(description = "Краткая информация о пользователе, находящаяся в публичном доступе")
@Log
public class PublicMainUserDataResponse {
    @Schema(description = "Идентификатор пользователя")
    private final long id;
    @Schema(description = "Отображаемое имя пользователя")
    private final String nickname;
    @Schema(description = "Фотография профиля пользователя", nullable = true)
    private final String photo;
    @Schema(description = "Дата последнего посещения или null в случае регистрации, но отстутствии авторизации",
            nullable = true)
    private final String lastVisit;

    public PublicMainUserDataResponse(User user, int zoneId){
        nickname = user.getNickname();
        id = user.getUserId();
        log.info("From DB got " + (user.getPhoto() == null? 0: user.getPhoto().length + " bytes"));
        photo = (user.getPhoto() == null? null: "data:" + user.getContentTypePhoto() + ";base64," +
                new String(Base64.encodeBase64(user.getPhoto()), StandardCharsets.UTF_8));
        lastVisit = (user.getLastVisit() == 0? null: LocalDateTime
                .ofEpochSecond(user.getLastVisit(), 0, ZoneOffset.ofHours(zoneId)).toString());
        log.info("Send " + (photo == null? 0: photo.length()) + " bytes");
    }
}
