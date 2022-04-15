package ru.manager.ProgectManager.DTO.response.kanban;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class KanbanElements {
    private final List<KanbanElementMainDataResponse> elements;

    public KanbanElements(List<KanbanElement> input, int pageIndex, int rowCount){
        elements = input.stream()
                .skip(pageIndex)
                .limit(rowCount)
                .map(KanbanElementMainDataResponse::new).collect(Collectors.toList());
    }

    public KanbanElements(Set<KanbanElement> elementSet){
        elements = elementSet.stream().map(KanbanElementMainDataResponse::new).collect(Collectors.toList());
    }
}
