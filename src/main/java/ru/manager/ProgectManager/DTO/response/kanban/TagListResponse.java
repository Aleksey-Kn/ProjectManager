package ru.manager.ProgectManager.DTO.response.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.kanban.Tag;

import java.util.Set;

@Getter
@AllArgsConstructor
@Schema(description = "Возвращает список доступных в канбане тегов")
public class TagListResponse {
    @Schema(description = "Список тегов")
    private Set<Tag> tags;
}
