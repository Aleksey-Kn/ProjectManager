package ru.manager.ProgectManager.DTO.response.calendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;

@Getter
@Schema(description = "Очень краткая информация об элементе канбана")
public class ShortKanbanElementInfo {
    @Schema(description = "Идентификатор элемента")
    private final long id;
    @Schema(description = "Название элемента")
    private final String cardName;
    @Schema(description = "Возможность данного пользователя редактировать данный элемент")
    private final boolean canEdit;

    public ShortKanbanElementInfo(KanbanElement element, boolean canRedact) {
        id = element.getId();
        cardName = element.getName();
        canEdit = canRedact;
    }
}
