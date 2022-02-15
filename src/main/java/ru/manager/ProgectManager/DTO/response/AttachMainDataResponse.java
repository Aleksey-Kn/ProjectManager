package ru.manager.ProgectManager.DTO.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.KanbanAttachment;

@Getter
@Schema(description = "Возвращает информацию о файлах для возможности их дальнейшего скачивания")
public class AttachMainDataResponse {
    @Schema(description = "Идентификатор вложения")
    private final long id;
    @Schema(description = "Имя вложения")
    private final String filename;

    public AttachMainDataResponse(KanbanAttachment attachment){
        id = attachment.getId();
        filename = attachment.getFilename();
    }
}
