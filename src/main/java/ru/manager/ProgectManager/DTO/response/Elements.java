package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.manager.ProgectManager.entitys.KanbanElement;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class Elements {
    private final List<KanbanElementMainDataResponse> elements;

    public Elements(List<KanbanElement> input){
        elements = input.stream().map(KanbanElementMainDataResponse::new).collect(Collectors.toList());
    }
}
