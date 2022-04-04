package ru.manager.ProgectManager.DTO.response.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.kanban.KanbanAttachment;

import javax.activation.MimetypesFileTypeMap;

@Getter
@Schema(description = "Полная информация о вложении")
public class AttachAllDataResponse {
    @Schema(description = "Идентификатор вложения")
    private final long id;
    @Schema(description = "Название файла")
    private final String name;
    @Schema(description = "Тип данных вложения")
    private final String type;
    @Schema(description = "Контент в формате массива байтов")
    private final byte[] data;

    public AttachAllDataResponse(KanbanAttachment attachment){
        id = attachment.getId();
        name = attachment.getFilename();
        type = new MimetypesFileTypeMap().getContentType(name);
        data = attachment.getFileData();
    }
}
