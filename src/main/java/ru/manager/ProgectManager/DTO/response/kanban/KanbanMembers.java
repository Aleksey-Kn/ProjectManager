package ru.manager.ProgectManager.DTO.response.kanban;

import lombok.Data;
import ru.manager.ProgectManager.DTO.response.user.PublicMainUserDataResponse;

import java.util.List;

@Data
public class KanbanMembers {
    private List<PublicMainUserDataResponse> changingMembers;
    private List<PublicMainUserDataResponse> browsingMembers;
}
