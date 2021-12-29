package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.KanbanElement;
import ru.manager.ProgectManager.statics.PhotoCompressor;

import java.io.IOException;

@Getter
public class KanbanElementMainDataResponse {
    private final long id;
    private final int serialNumber;
    private final String name;
    private final String tag;
    private final byte[] photo;
    private final PublicUserDataResponse creator;
    private final PublicUserDataResponse lastRedactor;

    public KanbanElementMainDataResponse(KanbanElement kanbanElement) throws IOException {
        id = kanbanElement.getId();
        serialNumber = kanbanElement.getSerialNumber();
        name = kanbanElement.getName();
        tag = kanbanElement.getTag();
        photo = PhotoCompressor.compress(kanbanElement.getPhoto(), kanbanElement.getExtension());
        creator = new PublicUserDataResponse(kanbanElement.getOwner());
        lastRedactor = new PublicUserDataResponse(kanbanElement.getLastRedactor());
    }
}
