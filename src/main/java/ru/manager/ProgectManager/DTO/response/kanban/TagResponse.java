package ru.manager.ProgectManager.DTO.response.kanban;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.kanban.Tag;

@Getter
public class TagResponse {
    private final long id;
    private final String text;
    private final String color;

    public TagResponse(Tag tag) {
        id = tag.getId();
        text = tag.getText();
        color = tag.getColor();
    }
}
