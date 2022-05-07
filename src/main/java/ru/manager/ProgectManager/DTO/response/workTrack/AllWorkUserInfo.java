package ru.manager.ProgectManager.DTO.response.workTrack;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Schema(description = "Отчёт о работе, проделанной пользователем в указанном проекте")
public class AllWorkUserInfo {
    @Schema(description = "Элементы канбана, с которыми ассоциирована выполненная работа")
    private Set<ElementWithWorkResponse> tasks;
    @Schema(description = "Суммарное количество работы для каждого рабочего дня")
    private List<WorkTrackShortResponse> workInConcreteDay;
    @Schema(description = "Сумарное количество работы в минутах за запрашиваемый диапазон")
    private int summaryWorkInDiapason;
}
