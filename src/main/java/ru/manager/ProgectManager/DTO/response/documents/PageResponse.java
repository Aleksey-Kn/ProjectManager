package ru.manager.ProgectManager.DTO.response.documents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.documents.Page;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Возвращаемая информация о странице документов")
public class PageResponse {
    @Schema(description = "Идентификатор страницы")
    private final long id;
    @Schema(description = "Название страницы")
    private final String name;
    @Schema(description = "Содержимое документа")
    private final String content;
    @Schema(description = "Дата и время последнего обновления")
    private final String updateTime;
    @Schema(description = "Флаг того, опубликована ли страница")
    private final boolean published;
    @Schema(description = "Порядковый номер в списке подстраниц родительской страницы")
    private final short serialNumber;
    @Schema(description = "Идентификаторы подстраниц данной страницы")
    private final List<Long> subpagesId;

    public PageResponse(Page page, int zoneId){
        id = page.getId();
        name = page.getName();
        content = page.getContent();
        updateTime = LocalDateTime
                .ofEpochSecond(page.getUpdateTime(), 0, ZoneOffset.ofHours(zoneId)).toString();
        published = page.isPublished();
        serialNumber = page.getSerialNumber();
        subpagesId = page.getSubpages().stream()
                .sorted(Comparator.comparing(Page::getSerialNumber))
                .map(Page::getId)
                .collect(Collectors.toList());
    }
}
