package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Validated
@Schema(description = "Запрос на создание комментария внутри элемента канбана")
public class KanbanCommentRequest {
    @Schema(description = "Идентификатор элемента в случае добавления комментария, " +
            "и идентификатор комментария в случае зароса на редактирование комментария", required = true)
    private long id;
    @NotBlank(message = "TEXT_MUST_BE_CONTAINS_VISIBLE_SYMBOL")
    @Size(max = 200, message = "TEXT_LENGTH_IS_TOO_LONG")
    @Schema(description = "Текст комментария")
    private String text;
    @Schema(description = "Часовой пояс текущего пользователя")
    private int zone;
}
