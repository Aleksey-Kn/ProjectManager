package ru.manager.ProgectManager.DTO.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Schema(description = "Полный список участников проекта")
public class UserDataListResponse {
    @Schema(description = "Список участников проекта")
    private final List<UserDataWithProjectRoleResponse> participants;
}
