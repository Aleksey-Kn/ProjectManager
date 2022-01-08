package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.KanbanColumn;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Возвращаемая информация о канбане")
public class KanbanResponse {
    @Schema(description = "Список колонок канбана")
    private final List<KanbanColumnResponse> kanbanColumns;

    public KanbanResponse(List<KanbanColumn> data){
        kanbanColumns = data.stream().map(KanbanColumnResponse::new).collect(Collectors.toList());
    }
}
