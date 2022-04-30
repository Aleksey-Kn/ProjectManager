package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.user.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Полный список участников проекта")
public class UserDataListResponse {
    @Schema(description = "Список участников проекта")
    private final List<PublicUserDataResponse> participants;

    public UserDataListResponse(Set<User> users, int zoneId){
        participants = users.parallelStream().map(user -> new PublicUserDataResponse(user, zoneId))
                .collect(Collectors.toList());
    }
}
