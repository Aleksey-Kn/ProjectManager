package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.KanbanElement;
import ru.manager.ProgectManager.entitys.KanbanElementComment;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Ответ предоставления полной информации о элементе канбана")
public class KanbanElementContentResponse {
    @Schema(description = "Текстовая информация, хранящаяся внутри элемента", nullable = true)
    private final String content;
    @Schema(description = "Идентификатор элемента")
    private final long id;
    @Schema(description = "Порядковый номер элемента в столбце")
    private final int serialNumber;
    @Schema(description = "Название элемента")
    private final String name;
    @Schema(description = "Тег элемента")
    private final String tag;
    @Schema(description = "Картинка, прикреплённа к элементу", nullable = true)
    private final byte[] photo;
    @Schema(description = "Информация об аккаунте создателя ячейки")
    private final PublicUserDataResponse creator;
    @Schema(description = "Информация об акаунте последнего редактора ячейки")
    private final PublicUserDataResponse lastRedactor;
    @Schema(description = "Комментарии")
    private final List<KanbanElementCommentResponse> comments;

    public KanbanElementContentResponse(KanbanElement kanbanElement) {
        id = kanbanElement.getId();
        serialNumber = kanbanElement.getSerialNumber();
        name = kanbanElement.getName();
        tag = kanbanElement.getTag();
        photo = kanbanElement.getPhoto();
        creator = new PublicUserDataResponse(kanbanElement.getOwner());
        lastRedactor = new PublicUserDataResponse(kanbanElement.getLastRedactor());
        content = kanbanElement.getContent();
        comments = kanbanElement.getComments().stream()
                .sorted(Comparator.comparing(KanbanElementComment::getId))
                .map(KanbanElementCommentResponse::new)
                .collect(Collectors.toList());
    }
}
