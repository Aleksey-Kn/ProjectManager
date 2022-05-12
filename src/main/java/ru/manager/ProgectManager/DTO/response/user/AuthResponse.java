package ru.manager.ProgectManager.DTO.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Ответ для предоставления токенов доступа к ресурсу")
public class AuthResponse {
    @Schema(description = "Refresh токен")
    private String refresh;
    @Schema(description = "Access токен")
    private String access;
}