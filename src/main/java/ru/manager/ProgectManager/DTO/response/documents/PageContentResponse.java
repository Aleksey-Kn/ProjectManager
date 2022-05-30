package ru.manager.ProgectManager.DTO.response.documents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.documents.Page;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Schema(description = "Данные запрашиваемого ресурса")
public class PageContentResponse {
    @Schema(description = "Данные документа запрашиваемой страницы")
    private final String content;
    @Schema(description = "Дата последнего изменения ресурса")
    private final String updateDate;
    @Schema(description = "Может ли пользователь изменять информацию в данной странице")
    private final boolean canEdit;

    public PageContentResponse(Page page, int zoneId, boolean canEditDocument) {
        content = page.getContent();
        updateDate = LocalDateTime
                .ofEpochSecond(page.getUpdateTime(), 0, ZoneOffset.ofHours(zoneId)).toString();
        canEdit = canEditDocument;
    }
}
