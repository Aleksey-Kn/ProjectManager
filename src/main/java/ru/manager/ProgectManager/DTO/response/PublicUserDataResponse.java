package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.User;

@Getter
public class PublicUserDataResponse {
    private final long id;
    private final String email;
    private final String nickname;
    private final byte[] photo;

    public PublicUserDataResponse(User user){
        email = user.getEmail();
        nickname = user.getNickname();
        id = user.getUserId();
        photo = user.getPhoto();
    }
}
