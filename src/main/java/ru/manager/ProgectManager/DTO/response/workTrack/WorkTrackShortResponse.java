package ru.manager.ProgectManager.DTO.response.workTrack;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.manager.ProgectManager.entitys.user.WorkTrack;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class WorkTrackShortResponse {
    @Schema(description = "Дата, в которую была совершена работа")
    private final String workDate;
    @Schema(description = "Количество времени, затраченного на задачу, в минутах")
    private final int workTime;

    public WorkTrackShortResponse(WorkTrack workTrack) {
        workDate = LocalDate.ofEpochDay(workTrack.getWorkDate()).toString();
        workTime = workTrack.getWorkTime();
    }
}
