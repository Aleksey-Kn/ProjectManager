package ru.manager.ProgectManager.DTO.response.documents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.documents.Page;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Список страниц документов")
public class PageResponseList {
    @Schema(description = "Список страниц документов")
    private final List<PageResponse> pages;

    public PageResponseList(List<Page> pageList, int zoneId, int pageIndex, int rowCount) {
        pages = pageList.stream()
                .skip(pageIndex)
                .limit(rowCount)
                .map(page -> new PageResponse(page, zoneId))
                .collect(Collectors.toList());
    }

    public PageResponseList(Set<Page> pageSet, int zoneId, int pageIndex, int rowCount) {
        pages = pageSet.stream()
                .skip(pageIndex)
                .limit(rowCount)
                .map(page -> new PageResponse(page, zoneId))
                .collect(Collectors.toList());
    }
}
