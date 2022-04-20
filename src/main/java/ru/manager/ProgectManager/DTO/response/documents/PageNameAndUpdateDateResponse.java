package ru.manager.ProgectManager.DTO.response.documents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.documents.Page;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Schema(description = "Информация о названии и последнем обновлении страницы")
public class PageNameAndUpdateDateResponse {
    @Schema(description = "Идентификатор страницы")
    private final long id;
    @Schema(description = "Название страницы")
    private final String name;
    @Schema(description = "Флаг того, опубликована ли страница")
    private final boolean published;
    @Schema(description = "Дата и время последнего обновления")
    private final String updateDate;

    public PageNameAndUpdateDateResponse(Page page, int zoneId) {
        id = page.getId();
        name = page.getName();
        published = page.isPublished();
        updateDate = LocalDateTime
                .ofEpochSecond(page.getUpdateTime(), 0, ZoneOffset.ofHours(zoneId)).toString();
    }
}
