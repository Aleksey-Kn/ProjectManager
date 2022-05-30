package ru.manager.ProgectManager.DTO.response.kanban;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.DTO.response.user.PublicMainUserDataResponse;
import ru.manager.ProgectManager.DTO.response.workTrack.WorkTrackAllResponse;
import ru.manager.ProgectManager.entitys.kanban.CheckBox;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.entitys.kanban.KanbanElementComment;
import ru.manager.ProgectManager.entitys.kanban.Tag;
import ru.manager.ProgectManager.entitys.user.WorkTrack;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Ответ предоставления полной информации о элементе канбана")
public class KanbanElementContentResponse {
    @Schema(description = "Текстовая информация, хранящаяся внутри элемента", nullable = true)
    private final String content;
    @Schema(description = "Идентификатор элемента")
    private final long id;
    @Schema(description = "Название элемента")
    private final String name;
    @Schema(description = "Теги элемента")
    private final Set<Tag> tags;
    @Schema(description = "Информация об аккаунте создателя ячейки")
    private final PublicMainUserDataResponse creator;
    @Schema(description = "Информация об акаунте последнего редактора ячейки")
    private final PublicMainUserDataResponse lastRedactor;
    @Schema(description = "Комментарии")
    private final List<KanbanElementCommentResponse> comments;
    @Schema(description = "Список вложенных файлов")
    private final List<AttachMainDataResponse> attachmentNames;
    @Schema(description = "Список чекбокстов элемента")
    private final Set<CheckBox> checkboxes;
    @Schema(description = "Дата и время создания элемента")
    private final String createDate;
    @Schema(description = "Дата и время последнего изменения элемента")
    private final String updateDate;
    @Schema(description = "Выбранная пользователем дата")
    private final String selectedDate;
    @Schema(description = "Список зафиксированных работ")
    private final List<WorkTrackAllResponse> workTracks;

    public KanbanElementContentResponse(KanbanElement kanbanElement, int zoneId) {
        id = kanbanElement.getId();
        name = kanbanElement.getName();
        tags = kanbanElement.getTags();
        creator = new PublicMainUserDataResponse(kanbanElement.getOwner(), zoneId);
        lastRedactor = new PublicMainUserDataResponse(kanbanElement.getLastRedactor(), zoneId);
        content = kanbanElement.getContent();
        selectedDate = kanbanElement.getSelectedDate() == 0? null:
                LocalDate.ofEpochDay(kanbanElement.getSelectedDate()).toString();
        comments = (kanbanElement.getComments() == null? List.of():
                kanbanElement.getComments().stream()
                .sorted(Comparator.comparing(KanbanElementComment::getId))
                .map(o -> new KanbanElementCommentResponse(o, zoneId))
                .collect(Collectors.toList()));
        attachmentNames = (kanbanElement.getKanbanAttachments() == null? List.of():
                kanbanElement.getKanbanAttachments().stream().map(AttachMainDataResponse::new)
                        .collect(Collectors.toList()));
        checkboxes = kanbanElement.getCheckBoxes();
        createDate = LocalDateTime
                .ofEpochSecond(kanbanElement.getTimeOfCreate(), 0, ZoneOffset.ofHours(zoneId)).toString();
        updateDate = LocalDateTime
                .ofEpochSecond(kanbanElement.getTimeOfUpdate(), 0, ZoneOffset.ofHours(zoneId)).toString();
        workTracks = kanbanElement.getWorkTrackSet().parallelStream()
                .sorted(Comparator.comparing(WorkTrack::getWorkDate).reversed())
                .map(WorkTrackAllResponse::new)
                .collect(Collectors.toList());
    }
}
