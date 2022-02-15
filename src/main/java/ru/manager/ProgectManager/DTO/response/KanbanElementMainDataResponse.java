package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.KanbanElement;

@Getter
@Schema(description = "Ответ, содержащий основную информацию об элементе канбана")
public class KanbanElementMainDataResponse {
    @Schema(description = "Идентификатор элемента")
    private final long id;
    @Schema(description = "Порядковый номер элемента в колонке")
    private final int serialNumber;
    @Schema(description = "Название элемента")
    private final String name;
    @Schema(description = "Тег элемента")
    private final String tag;
    @Schema(description = "Информация об аккаунте создателя ячейки")
    private final PublicUserDataResponse creator;
    @Schema(description = "Информация об акаунте последнего редактора ячейки")
    private final PublicUserDataResponse lastRedactor;
    @Schema(description = "Количество комментариев в ячейке")
    private final int commentCount;
    @Schema(description = "Количество вложений в ячейке")
    private final int attachCount;

    public KanbanElementMainDataResponse(KanbanElement kanbanElement) {
        id = kanbanElement.getId();
        serialNumber = kanbanElement.getSerialNumber();
        name = kanbanElement.getName();
        tag = kanbanElement.getTag();
        creator = new PublicUserDataResponse(kanbanElement.getOwner());
        lastRedactor = new PublicUserDataResponse(kanbanElement.getLastRedactor());
        commentCount = (kanbanElement.getComments() == null? 0: kanbanElement.getComments().size());
        attachCount = (kanbanElement.getKanbanAttachments() == null? 0: kanbanElement.getKanbanAttachments().size());
    }
}
