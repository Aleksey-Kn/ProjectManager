package ru.manager.ProgectManager.DTO.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.user.User;

@Getter
@Schema(description = "Информация о пользователя о самом себе")
public class MyselfUserDataResponse {
    @Schema(description = "Логин пользователя")
    private final String login;
    @Schema(description = "Адрес электронной почты пользователя")
    private final String email;
    @Schema(description = "Отображаемое имя пользователя")
    private final String nickname;
    @Schema(description = "Фотография профиля пользователя", nullable = true)
    private final byte[] photo;
    @Schema(description = "Тип данных фотографии")
    private final String datatypePhoto;

    public MyselfUserDataResponse(User user){
        email = user.getEmail();
        nickname = user.getNickname();
        login = user.getUsername();
        photo = user.getPhoto();
        datatypePhoto = user.getContentTypePhoto();
    }
}
