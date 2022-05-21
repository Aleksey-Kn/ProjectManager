package ru.manager.ProgectManager.DTO.response.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.DTO.response.user.PublicMainUserDataResponse;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.enums.TypeRoleProject;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Основная информация о канбане и его участниках")
public class KanbanMainDataResponse {
    @Schema(description = "Идентификатор канбана")
    private final long id;
    @Schema(description = "Название канбана")
    private final String name;
    @Schema(description = "Список пользователей, имеющих доступ к данной доске")
    private final List<PublicMainUserDataResponse> participants;
    @Schema(description = "Ссылка на изображение, прикрелённое к канбану", nullable = true)
    private final String image;

    public KanbanMainDataResponse(Kanban kanban, int zoneId){
        id = kanban.getId();
        name = kanban.getName();
        participants = kanban.getProject().getConnectors().parallelStream()
                .filter(c -> c.getRoleType() != TypeRoleProject.CUSTOM_ROLE || c.getCustomProjectRole()
                        .getCustomRoleWithKanbanConnectors().parallelStream().anyMatch(kc -> kc.getKanban().equals(kanban)))
                .map(UserWithProjectConnector::getUser)
                .map(user -> new PublicMainUserDataResponse(user, zoneId))
                .limit(3)
                .collect(Collectors.toList());
        image = (kanban.getPhoto() == null? null: "https://api.veehark.xyz/photo/kanban?id=" + kanban.getId());
    }
}
