package ru.manager.ProgectManager.DTO.response.calendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
@Schema(description = "Информация о карточках, принадлежащих к дню месяца")
public class CalendarResponse {
    @Schema(description = "День, к которому принадлежат карточки канбана")
    private final String date;
    @Schema(description = "Список карточек, принадлежащих к данному дню")
    private final Set<ShortKanbanElementInfo> cards;
}
