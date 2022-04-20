package ru.manager.ProgectManager.DTO.response.documents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Список информации о имени обновлении страниц")
public class PageNameResponseList {
    @Schema(description = "Список страниц")
    private final List<PageNameResponse> pages;

    public PageNameResponseList(Set<PageNameResponse> pageSet, int pageIndex, int rowCount) {
        pages = pageSet.stream()
                .skip(pageIndex)
                .limit(rowCount)
                .collect(Collectors.toList());
    }
}
