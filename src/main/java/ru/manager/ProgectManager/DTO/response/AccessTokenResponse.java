package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "Ответ для предоставления access токена")
public class AccessTokenResponse {
    @Schema(description = "Access токен")
    private final String access;
}
