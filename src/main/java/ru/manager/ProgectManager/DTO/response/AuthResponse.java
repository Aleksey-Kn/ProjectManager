package ru.manager.ProgectManager.DTO.response;

import lombok.Data;

@Data
public class AuthResponse {
    private String access;
    private String refresh;
}