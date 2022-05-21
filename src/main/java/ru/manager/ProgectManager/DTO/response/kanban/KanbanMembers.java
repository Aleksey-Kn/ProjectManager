package ru.manager.ProgectManager.DTO.response.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.manager.ProgectManager.DTO.response.user.PublicMainUserDataResponse;

import java.util.List;

@Data
@Schema(description = "Информация об участниках канбана")
public class KanbanMembers {
    @Schema(description = "Участники с правом изменения канбана")
    private List<PublicMainUserDataResponse> changingMembers;
    @Schema(description = "Участники с правом просмотра канбана")
    private List<PublicMainUserDataResponse> browsingMembers;
}
