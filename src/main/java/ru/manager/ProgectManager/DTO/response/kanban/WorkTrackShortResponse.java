package ru.manager.ProgectManager.DTO.response.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.user.WorkTrack;

import java.time.LocalDate;

@Getter
@Schema(description = "Данные об отработанном времени")
public class WorkTrackShortResponse {
    @Schema(description = "Дата, в которую была совершена работа")
    private final String workDate;
    @Schema(description = "Количество времени, затраченное на задачу")
    private final int workTime;
    @Schema(description = "Коментарий к выполненному объёму работы")
    private final String comment;
    @Schema(description = "Идентификатор пользователя, выполнившего работу")
    private final long userId;

    public WorkTrackShortResponse(WorkTrack workTrack) {
        workDate = LocalDate.ofEpochDay(workTrack.getWorkDate()).toString();
        workTime = workTrack.getWorkTime();
        comment = workTrack.getComment();
        userId = workTrack.getOwner().getUserId();
    }
}
