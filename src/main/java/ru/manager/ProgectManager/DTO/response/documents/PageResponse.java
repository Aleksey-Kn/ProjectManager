package ru.manager.ProgectManager.DTO.response.documents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.documents.Page;

@Getter
@Schema(description = "Возвращаемая информация о странице документов")
public class PageResponse {
    @Schema(description = "Идентификатор страницы")
    private final long id;
    @Schema(description = "Название страницы")
    private final String name;
    @Schema(description = "Флаг того, опубликована ли страница")
    private final boolean published;
    @Schema(description = "Порядковый номер в списке подстраниц родительской страницы")
    private final short serialNumber;
    @Schema(description = "Уровень вложенности текущей страницы")
    private final int nestingLevel;

    public PageResponse(Page page){
        id = page.getId();
        name = page.getName();
        published = page.isPublished();
        serialNumber = page.getSerialNumber();
        nestingLevel = findNestingLevel(0, page);
    }

    private int findNestingLevel(int nowLevel, Page nowPage) {
        return nowPage.getSubpages().stream()
                .mapToInt(p -> findNestingLevel(nowLevel + 1, p))
                .max()
                .orElse(nowLevel);
    }
}
