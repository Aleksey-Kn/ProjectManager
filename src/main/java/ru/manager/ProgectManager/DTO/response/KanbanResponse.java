package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.KanbanColumn;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class KanbanResponse {
    private final List<KanbanColumnResponse> kanbanColumns;

    public KanbanResponse(List<KanbanColumn> data){
        kanbanColumns = data.stream().map(KanbanColumnResponse::new).collect(Collectors.toList());
    }
}
