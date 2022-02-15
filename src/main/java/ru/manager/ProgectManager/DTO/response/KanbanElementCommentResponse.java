package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.KanbanElementComment;

@Getter
@Schema(description = "Возвращаемые данные о комментарии элемента канбана")
public class KanbanElementCommentResponse {
    @Schema(description = "Идентификатор комментария")
    private final long id;
    @Schema(description = "Текст комментария")
    private final String text;
    @Schema(description = "Данные об авторе комментария")
    private final PublicUserDataResponse owner;

    public KanbanElementCommentResponse(KanbanElementComment comment){
        id = comment.getId();
        text = comment.getText();
        owner = new PublicUserDataResponse(comment.getOwner());
    }
}
