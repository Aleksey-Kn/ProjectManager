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

    public PageNameResponse(Page page){
        id = page.getId();
        name = page.getName();
        published = page.isPublished();
    }
}
