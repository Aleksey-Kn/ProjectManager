package ru.manager.ProgectManager.DTO.response.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.kanban.KanbanColumn;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode
@SuperBuilder
@Schema(description = "Информация о канбане и его содержимом")
public class KanbanContentResponse {
    @Schema(description = "Название канбана")
    private final String name;
    @Schema(description = "Идентификатор канбана")
    private final long id;
    @Schema(description = "Может ли пользовватель изменять информацию в данной доске")
    private final boolean canEdit;
    @Schema(description = "Список колонок канбана")
    private final List<KanbanColumn> kanbanColumns;
    @Schema(description = "Ссылка на изображение, прикрелённое к канбану", nullable = true)
    private final String image;

    public KanbanContentResponse(Kanban kanban, int pageIndex, int count, boolean canEditKanban){
        kanbanColumns = kanban.getKanbanColumns().stream()
                .sorted(Comparator.comparing(KanbanColumn::getSerialNumber))
                .skip(pageIndex)
                .limit(count)
                .collect(Collectors.toList());
        name = kanban.getName();
        id = kanban.getId();
        canEdit = canEditKanban;
        image = (kanban.getPhoto() == null? null: "https://api.veehark.xyz/photo/kanban?id=" + kanban.getId());
    }
}
