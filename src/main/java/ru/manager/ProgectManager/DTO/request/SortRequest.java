package ru.manager.ProgectManager.DTO.request;

import lombok.Getter;
import ru.manager.ProgectManager.enums.SortType;

@Getter
public class SortRequest {
    private long id;
    private SortType type;
    private boolean reverse;
}
