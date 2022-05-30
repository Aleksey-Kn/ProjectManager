package ru.manager.ProgectManager.DTO.response.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class UserDataForAdminList {
    private final List<UserDataForAdmin> users;
}
