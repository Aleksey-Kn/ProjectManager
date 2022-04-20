package ru.manager.ProgectManager.DTO.response.documents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Список страниц документов")
public class PageResponseList {
    @Schema(description = "Список страниц документов")
    private final List<PageResponse> pages;

    public PageResponseList(List<PageResponse> pageList, int pageIndex, int rowCount) {
        pages = pageList.stream()
                .skip(pageIndex)
                .limit(rowCount)
                .collect(Collectors.toList());
    }

    public PageResponseList(Set<PageResponse> pageSet, int pageIndex, int rowCount) {
        pages = pageSet.stream()
                .skip(pageIndex)
                .limit(rowCount)
                .collect(Collectors.toList());
    }
}
