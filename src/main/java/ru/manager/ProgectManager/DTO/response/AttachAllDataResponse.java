package ru.manager.ProgectManager.DTO.response;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.KanbanAttachment;

import javax.activation.MimetypesFileTypeMap;

@Getter
public class AttachAllDataResponse {
    private final long id;
    private final String name;
    private final String type;
    private final byte[] data;

    public AttachAllDataResponse(KanbanAttachment attachment){
        id = attachment.getId();
        name = attachment.getFilename();
        type = new MimetypesFileTypeMap().getContentType(name);
        data = attachment.getFileData();
    }
}
