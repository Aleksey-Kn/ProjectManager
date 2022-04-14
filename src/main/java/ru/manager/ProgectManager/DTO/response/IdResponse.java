package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "Идентификатор созданного ресурса")
public class IdResponse {
    @Schema(description = "Идентификатор ресурса")
    private final long id;
}
