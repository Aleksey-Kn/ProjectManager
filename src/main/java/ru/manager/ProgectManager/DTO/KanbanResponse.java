package ru.manager.ProgectManager.DTO;

import lombok.AllArgsConstructor;
import ru.manager.ProgectManager.entitys.KanbanColumn;

import java.util.List;

@AllArgsConstructor
public class KanbanResponse {
    private List<KanbanColumn> kanbanColumns;
}
