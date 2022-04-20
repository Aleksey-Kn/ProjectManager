package ru.manager.ProgectManager.DTO.response.documents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Список информации о имени и последем обновлении страниц")
public class PageNameAndUpdateDateResponseList {
    @Schema(description = "Список страниц")
    private final List<PageNameAndUpdateDateResponse> pages;

    public PageNameAndUpdateDateResponseList(List<PageNameAndUpdateDateResponse> pageList, int pageIndex, int rowCount) {
        pages = pageList.stream()
                .skip(pageIndex)
                .limit(rowCount)
                .collect(Collectors.toList());
    }
}
