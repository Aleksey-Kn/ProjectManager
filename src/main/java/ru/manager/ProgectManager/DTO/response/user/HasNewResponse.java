package ru.manager.ProgectManager.DTO.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "Информация о наличии нового контента")
public class HasNewResponse {
    @Schema(description = "Наличие нового контента")
    private final boolean hasNew;
}
