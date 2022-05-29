package ru.manager.ProgectManager.DTO.response.calendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
@Schema(description = "Список дней, к которым привязаны карточки канбана")
public class CalendarResponseList {
    @Schema(description = "Список дней с принадлежащими к ним карточками")
    private final Set<CalendarResponse> daysWithContent;
}
