package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Запрос на обновление токенов")
public class RefreshTokenRequest {
    @Schema(required = true, description = "Текущий refresh токен")
    private String refresh;
}
