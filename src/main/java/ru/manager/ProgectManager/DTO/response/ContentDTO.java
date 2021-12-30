package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.KanbanElement;

@Getter
public class ContentDTO {
    private final String content;
    private final long id;
    private final int serialNumber;
    private final String name;
    private final String tag;
    private final byte[] photo;
    private final PublicUserDataResponse creator;
    private final PublicUserDataResponse lastRedactor;

    public ContentDTO(KanbanElement kanbanElement) {
        id = kanbanElement.getId();
        serialNumber = kanbanElement.getSerialNumber();
        name = kanbanElement.getName();
        tag = kanbanElement.getTag();
        photo = kanbanElement.getPhoto();
        creator = new PublicUserDataResponse(kanbanElement.getOwner());
        lastRedactor = new PublicUserDataResponse(kanbanElement.getLastRedactor());
        content = kanbanElement.getContent();
    }
}
