package ru.manager.ProgectManager.DTO.response.workTrack;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.entitys.user.WorkTrack;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Информация об элементе, с которым ассоциирована работа")
public class ElementWithWorkResponse {
    @Schema(description = "Идентификатор элемента")
    private final long id;
    @Schema(description = "Название элемента")
    private final String name;
    @Schema(description = "Список выполненных работ, ассоциированных с данным элементом")
    private final List<WorkTrackShortResponse> works;

    public ElementWithWorkResponse(KanbanElement kanbanElement, User user, LocalDate from, LocalDate to) {
        id = kanbanElement.getId();
        name = kanbanElement.getName();
        works = kanbanElement.getWorkTrackSet().stream()
                .filter(workTrack -> workTrack.getOwner().equals(user))
                .filter(workTrack -> LocalDate.ofEpochDay(workTrack.getWorkDate()).isAfter(from))
                .filter(workTrack -> LocalDate.ofEpochDay(workTrack.getWorkDate()).isBefore(to))
                .sorted(Comparator.comparing(WorkTrack::getWorkDate))
                .map(WorkTrackShortResponse::new)
                .collect(Collectors.toList());
    }
}
