package ru.manager.ProgectManager.DTO.response.documents;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.documents.Page;

@Getter
@Schema(description = "Информация о названии страницы")
public class PageNameResponse {
    @Schema(description = "Идентификатор страницы")
    private final long id;
    @Schema(description = "Название страницы")
    private final String name;
    @Schema(description = "Флаг того, опубликована ли страница")
    private final boolean published;
    @Schema(description = "Может ли пользователь изменять информацию в данной странице")
    private final boolean canEdit;

    public PageNameResponse(Page page, boolean canEditDocument){
        id = page.getId();
        name = page.getName();
        published = page.isPublished();
        canEdit = canEditDocument;
    }
}
