package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.KanbanElement;

@Getter
public class KanbanElementMainDataResponse {
    private final long id;
    private final int serialNumber;
    private final String name;
    private final String tag;
    private final String content;
    private final byte[] photo;
    private final PublicUserDataResponse creator;
    private final PublicUserDataResponse lastRedactor;

    public KanbanElementMainDataResponse(KanbanElement kanbanElement){
        id = kanbanElement.getId();
        serialNumber = kanbanElement.getSerialNumber();
        name = kanbanElement.getName();
        tag = kanbanElement.getTag();
        content = kanbanElement.getContent();
        photo = kanbanElement.getPhoto();
        creator = new PublicUserDataResponse(kanbanElement.getOwner());
        lastRedactor = new PublicUserDataResponse(kanbanElement.getLastRedactor());
    }
}
