package ru.manager.ProgectManager.DTO.response.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.DTO.response.PublicUserDataResponse;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.entitys.kanban.Tag;

import java.util.Set;

@Getter
@Schema(description = "Ответ, содержащий основную информацию об элементе канбана")
public class KanbanElementMainDataResponse {
    @Schema(description = "Идентификатор элемента")
    private final long id;
    @Schema(description = "Порядковый номер элемента в колонке")
    private final int serialNumber;
    @Schema(description = "Название элемента")
    private final String name;
    @Schema(description = "Теги элемента")
    private final Set<Tag> tags;
    @Schema(description = "Информация об аккаунте создателя ячейки")
    private final PublicUserDataResponse creator;
    @Schema(description = "Информация об акаунте последнего редактора ячейки")
    private final PublicUserDataResponse lastRedactor;
    @Schema(description = "Количество комментариев в ячейке")
    private final int commentCount;
    @Schema(description = "Количество вложений в ячейке")
    private final int attachCount;
    @Schema(description = "Выбранная пользователем дата")
    private final String selectedDate;

    public KanbanElementMainDataResponse(KanbanElement kanbanElement, int zoneId) {
        id = kanbanElement.getId();
        serialNumber = kanbanElement.getSerialNumber();
        name = kanbanElement.getName();
        tags = kanbanElement.getTags();
        selectedDate = kanbanElement.getSelectedDate();
        creator = new PublicUserDataResponse(kanbanElement.getOwner(), zoneId);
        lastRedactor = new PublicUserDataResponse(kanbanElement.getLastRedactor(), zoneId);
        commentCount = (kanbanElement.getComments() == null? 0: kanbanElement.getComments().size());
        attachCount = (kanbanElement.getKanbanAttachments() == null? 0: kanbanElement.getKanbanAttachments().size());
    }
}
