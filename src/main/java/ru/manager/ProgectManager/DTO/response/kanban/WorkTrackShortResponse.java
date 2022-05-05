package ru.manager.ProgectManager.DTO.response.kanban;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.user.WorkTrack;

import java.time.LocalDate;

@Getter
public class WorkTrackShortResponse {
    private final String workDate;
    private final int workTime;
    private final String comment;
    private final long userId;

    public WorkTrackShortResponse(WorkTrack workTrack) {
        workDate = LocalDate.ofEpochDay(workTrack.getWorkDate()).toString();
        workTime = workTrack.getWorkTime();
        comment = workTrack.getComment();
        userId = workTrack.getOwner().getUserId();
    }
}
