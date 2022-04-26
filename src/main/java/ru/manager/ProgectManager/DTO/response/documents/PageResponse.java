package ru.manager.ProgectManager.DTO.response.documents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.documents.Page;
import ru.manager.ProgectManager.entitys.user.User;

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
    @Schema(description = "Флаг того, опубликована ли страница")
    private final boolean published;
    @Schema(description = "Порядковый номер в списке подстраниц родительской страницы")
    private final short serialNumber;
    @Schema(description = "Идентификаторы подстраниц данной страницы")
    private final List<Long> subpagesId;

    public PageResponse(Page page, User currentUser){
        id = page.getId();
        name = page.getName();
        published = page.isPublished();
        serialNumber = page.getSerialNumber();
        subpagesId = page.getSubpages().stream()
                .filter(p -> p.isPublished() || currentUser.equals(p.getOwner()))
                .sorted(Comparator.comparing(Page::getSerialNumber))
                .map(Page::getId)
                .collect(Collectors.toList());
    }
}
