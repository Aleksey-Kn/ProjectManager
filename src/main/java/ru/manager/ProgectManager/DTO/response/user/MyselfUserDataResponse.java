package ru.manager.ProgectManager.DTO.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.tomcat.util.codec.binary.Base64;
import ru.manager.ProgectManager.entitys.user.User;

import java.nio.charset.StandardCharsets;

@Getter
@Schema(description = "Информация о пользователя о самом себе")
@Log
public class MyselfUserDataResponse {
    @Schema(description = "Идентификатор пользователя")
    private final long id;
    @Schema(description = "Логин пользователя")
    private final String login;
    @Schema(description = "Адрес электронной почты пользователя")
    private final String email;
    @Schema(description = "Отображаемое имя пользователя")
    private final String nickname;
    @Schema(description = "Фотография профиля пользователя", nullable = true)
    private final String photo;

    public MyselfUserDataResponse(User user){
        id = user.getUserId();
        email = user.getEmail();
        nickname = user.getNickname();
        login = user.getUsername();
        log.info("From DB got " + (user.getPhoto() == null? 0: user.getPhoto().length + " bytes"));
        photo = (user.getPhoto() == null? null: "data:image/jpg;base64," +
                new String(Base64.encodeBase64(user.getPhoto()), StandardCharsets.UTF_8));
        log.info("Send " + (photo == null? 0: photo.length()) + " bytes");
    }
}
