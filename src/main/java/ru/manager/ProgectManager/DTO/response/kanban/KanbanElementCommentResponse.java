package ru.manager.ProgectManager.DTO.response.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.DTO.response.PublicUserDataResponse;
import ru.manager.ProgectManager.entitys.kanban.KanbanElementComment;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Schema(description = "Возвращаемые данные о комментарии элемента канбана")
public class KanbanElementCommentResponse {
    @Schema(description = "Идентификатор комментария")
    private final long id;
    @Schema(description = "Текст комментария")
    private final String text;
    @Schema(description = "Данные об авторе комментария")
    private final PublicUserDataResponse owner;
    @Schema(description = "Время послднего редактирования комментария")
    private final String dateTime;

    public KanbanElementCommentResponse(KanbanElementComment comment, int zoneId){
        id = comment.getId();
        text = comment.getText();
        owner = new PublicUserDataResponse(comment.getOwner());
        dateTime = LocalDateTime
                .ofEpochSecond(comment.getDateTime(), 0, ZoneOffset.ofHours(zoneId)).toString();
    }
}
