package ru.manager.ProgectManager.DTO;

import lombok.Data;

@Data
public class AuthRequest {
    private String login;
    private String password;
}
