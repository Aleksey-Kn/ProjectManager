package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.User;

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

    public PublicUserDataResponse(User user){
        email = user.getEmail();
        nickname = user.getNickname();
        id = user.getUserId();
        photo = user.getPhoto();
    }
}
