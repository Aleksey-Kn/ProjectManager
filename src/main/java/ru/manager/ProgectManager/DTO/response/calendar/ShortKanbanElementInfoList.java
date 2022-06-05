package ru.manager.ProgectManager.DTO.response.calendar;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ShortKanbanElementInfoList {
    private final List<ShortKanbanElementInfo> cards;
}
