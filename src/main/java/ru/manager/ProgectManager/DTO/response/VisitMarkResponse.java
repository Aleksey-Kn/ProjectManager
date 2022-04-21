package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.VisitMark;

@Getter
public class VisitMarkResponse {
    private final long id;
    private final String type;
    private final String name;

    public VisitMarkResponse(VisitMark visitMark) {
        id = visitMark.getResourceId();
        name = visitMark.getResourceName();
        type = visitMark.getResourceType().getStringValue();
    }
}
