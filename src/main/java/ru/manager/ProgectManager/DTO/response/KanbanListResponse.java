package ru.manager.ProgectManager.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.Kanban;

import java.util.List;

@Getter
@AllArgsConstructor
public class KanbanListResponse {
    private List<Kanban> kanbans;
}
