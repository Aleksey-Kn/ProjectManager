package ru.manager.ProgectManager.DTO.response.calendar;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
@Schema(description = "Инфрмация о карточках, привязанных к указанному дню")
public class ShortKanbanElementInfoList {
    @Schema(description = "Список карточек, привязанных к указанному дню")
    private final List<ShortKanbanElementInfo> cards;
}
