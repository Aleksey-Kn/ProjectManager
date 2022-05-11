package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Data
@Validated
@Schema(description = "Запрос на обновление токенов")
public class RefreshTokenRequest {
    @NotBlank
    @Schema(required = true, description = "Текущий refresh токен")
    private String refresh;
    @Schema(description = "Текущий часовой пояс пользователя", required = true)
    private int zoneId;
}
