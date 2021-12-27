package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.KanbanColumn;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class KanbanColumnResponse {
    private final long id;
    private final int serialNumber;
    private final String name;
    private final List<KanbanElementMainDataResponse> kanbanElements;

    public KanbanColumnResponse(KanbanColumn kanbanColumn){
        id = kanbanColumn.getId();
        serialNumber = kanbanColumn.getSerialNumber();
        name = kanbanColumn.getName();
        kanbanElements = kanbanColumn.getElements().stream()
                .map(KanbanElementMainDataResponse::new)
                .collect(Collectors.toList());
    }
}
