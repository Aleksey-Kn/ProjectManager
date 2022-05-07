package ru.manager.ProgectManager.DTO.response.workTrack;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.user.WorkTrack;

import java.time.LocalDate;

@Getter
@Schema(description = "Данные об отработанном времени")
public class WorkTrackAllResponse {
    @Schema(description = "Идентификатор отработанного времени")
    private final long id;
    @Schema(description = "Дата, в которую была совершена работа")
    private final String workDate;
    @Schema(description = "Количество времени, затраченного на задачу, в минутах")
    private final int workTime;
    @Schema(description = "Коментарий к выполненному объёму работы")
    private final String comment;
    @Schema(description = "Идентификатор пользователя, выполнившего работу")
    private final long userId;

    public WorkTrackAllResponse(WorkTrack workTrack) {
        id = workTrack.getId();
        workDate = LocalDate.ofEpochDay(workTrack.getWorkDate()).toString();
        workTime = workTrack.getWorkTime();
        comment = workTrack.getComment();
        userId = workTrack.getOwner().getUserId();
    }
}
