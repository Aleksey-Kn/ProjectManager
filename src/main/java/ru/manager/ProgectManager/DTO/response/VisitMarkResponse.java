package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.VisitMark;

@Getter
@Schema(description = "Информация о посещённом ресурсе")
public class VisitMarkResponse {
    @Schema(description = "Идентификатор посещённого ресурса")
    private final long id;
    @Schema(description = "Тип посещённого ресурса")
    private final String type;
    @Schema(description = "Название посещённого ресурса")
    private final String name;

    public VisitMarkResponse(VisitMark visitMark) {
        id = visitMark.getResourceId();
        name = visitMark.getResourceName();
        type = visitMark.getResourceType().getStringValue();
    }
}
