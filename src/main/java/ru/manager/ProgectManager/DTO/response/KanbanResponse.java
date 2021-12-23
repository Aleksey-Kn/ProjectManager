package ru.manager.ProgectManager.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.KanbanColumn;

import java.util.List;

@Getter
@AllArgsConstructor
public class KanbanResponse {
    private List<KanbanColumn> kanbanColumns;
}
