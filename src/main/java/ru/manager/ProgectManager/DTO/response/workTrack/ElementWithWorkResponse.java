package ru.manager.ProgectManager.DTO.response.workTrack;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.entitys.user.WorkTrack;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ElementWithWorkResponse {
    private final long id;
    private final String name;
    private final List<WorkTrackShortResponse> works;

    public ElementWithWorkResponse(KanbanElement kanbanElement, User user, LocalDate from, LocalDate to) {
        id = kanbanElement.getId();
        name = kanbanElement.getName();
        works = kanbanElement.getWorkTrackSet().parallelStream()
                .filter(workTrack -> workTrack.getOwner().equals(user))
                .filter(workTrack -> LocalDate.ofEpochDay(workTrack.getWorkDate()).isAfter(from))
                .filter(workTrack -> LocalDate.ofEpochDay(workTrack.getWorkDate()).isBefore(to))
                .sorted(Comparator.comparing(WorkTrack::getWorkDate))
                .map(WorkTrackShortResponse::new)
                .collect(Collectors.toList());
    }
}
