package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.KanbanElementComment;

@Getter
//TODO: документация
public class KanbanElementCommentResponse {
    private final long id;
    private final String text;
    private final PublicUserDataResponse owner;

    public KanbanElementCommentResponse(KanbanElementComment comment){
        id = comment.getId();
        text = comment.getText();
        owner = new PublicUserDataResponse(comment.getOwner());
    }
}
