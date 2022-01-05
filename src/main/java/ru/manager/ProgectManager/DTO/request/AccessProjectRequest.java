package ru.manager.ProgectManager.DTO.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Запрос на предоставление доступа к проекту")
public class AccessProjectRequest {
    @Schema(description = "Идентификатор проекта, к которому будет предоставлен доступ", required = true)
    private long projectId;
    @Schema(description = "Булевая величина, отображающая, будет ли приглашённый пользователь администратором",
            required = true)
    private boolean hasAdmin;
    @Schema(description = "Булевая величина, отображающая, будет ли сгенерированная ссылка многоразовой", required = true)
    private boolean disposable;
    @Schema(description = "Срок действия ссылки в днях. По умолчанию ссылка действет в течении дня")
    private int liveTimeInDays;
}
