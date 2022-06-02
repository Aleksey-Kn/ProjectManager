package ru.manager.ProgectManager.DTO.response.user;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.user.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class MainUserDataListResponse {
    private final List<PublicMainUserDataResponse> participants;

    public MainUserDataListResponse(Set<User> users, int zoneId) {
        participants = users.parallelStream()
                .map(user -> new PublicMainUserDataResponse(user, zoneId))
                .collect(Collectors.toList());
    }
}
