package ru.manager.ProgectManager.DTO.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.tomcat.util.codec.binary.Base64;
import ru.manager.ProgectManager.entitys.user.Note;
import ru.manager.ProgectManager.entitys.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Getter
@Schema(description = "Полная информация о запрашиваемом пользователе, доступная для публичного доступа")
@Log
public class PublicAllDataResponse {
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
    @Schema(description = "Заметка о данном пользователе, составленная текущим пользователем", nullable = true)
    private final String note;

    public PublicAllDataResponse(User user, int zoneId, Optional<Note> optionalNote){
        email = user.getEmail();
        nickname = user.getNickname();
        id = user.getUserId();
        log.info("From DB got " + (user.getPhoto() == null? 0: user.getPhoto().length + " bytes"));
        photo = (user.getPhoto() == null? null: "data:" + user.getContentTypePhoto() + ";base64," +
                new String(Base64.encodeBase64(user.getPhoto()), StandardCharsets.UTF_8));
        lastVisit = (user.getLastVisit() == 0? null: LocalDateTime
                .ofEpochSecond(user.getLastVisit(), 0, ZoneOffset.ofHours(zoneId)).toString());
        this.note = optionalNote.map(Note::getText).orElse(null);
        log.info("Send " + (photo == null? 0: photo.length()) + " bytes");
    }
}
