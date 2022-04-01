package ru.manager.ProgectManager.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.Kanban;

import java.util.Set;

@Getter
@AllArgsConstructor
public class KanbanListResponse {
    private Set<Kanban> kanbans;
}
