package ru.manager.ProgectManager.DTO.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Schema(description = "Список всех существующих аккаунтов в системе")
public class UserDataForAdminList {
    @Schema(description = "Список аккаунтов пользователей системы")
    private final List<UserDataForAdmin> users;
}
