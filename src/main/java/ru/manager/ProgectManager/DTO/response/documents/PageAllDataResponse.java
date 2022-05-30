package ru.manager.ProgectManager.DTO.response.documents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.documents.Page;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Schema(description = "Полная информация о странице документов")
public class PageAllDataResponse {
    @Schema(description = "Идентификатор страницы")
    private final long id;
    @Schema(description = "Название страницы")
    private final String name;
    @Schema(description = "Флаг того, опубликована ли страница")
    private final boolean published;
    @Schema(description = "Данные документа запрашиваемой страницы")
    private final String content;
    @Schema(description = "Дата последнего изменения ресурса")
    private final String updateDate;
    @Schema(description = "Может ли пользователь изменять информацию в данной странице")
    private final boolean canEdit;

    public PageAllDataResponse(Page page, int zoneId, boolean canEditDocument) {
        id = page.getId();
        name = page.getName();
        published = page.isPublished();
        updateDate = LocalDateTime
                .ofEpochSecond(page.getUpdateTime(), 0, ZoneOffset.ofHours(zoneId)).toString();
        content = page.getContent();
        canEdit = canEditDocument;
    }
}
