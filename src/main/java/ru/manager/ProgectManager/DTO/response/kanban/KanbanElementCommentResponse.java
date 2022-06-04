package ru.manager.ProgectManager.DTO.response.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.DTO.response.user.PublicMainUserDataResponse;
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
    private final PublicMainUserDataResponse owner;
    @Schema(description = "Время послднего редактирования комментария")
    private final String dateTime;
    @Schema(description = "Отрадактированность комментария")
    private final boolean redacted;

    public KanbanElementCommentResponse(KanbanElementComment comment, int zoneId){
        id = comment.getId();
        text = comment.getText();
        owner = new PublicMainUserDataResponse(comment.getOwner(), zoneId);
        dateTime = LocalDateTime
                .ofEpochSecond(comment.getDateTime(), 0, ZoneOffset.ofHours(zoneId)).toString();
        redacted = comment.isRedacted();
    }
}
