package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.KanbanElement;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class Elements {
    private final List<KanbanElementMainDataResponse> elements;

    public Elements(List<KanbanElement> input, int pageIndex, int rowCount){
        elements = input.stream()
                .skip(pageIndex)
                .limit(rowCount)
                .map(KanbanElementMainDataResponse::new).collect(Collectors.toList());
    }
}
