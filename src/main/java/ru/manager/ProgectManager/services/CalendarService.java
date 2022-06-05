package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.response.calendar.CalendarResponse;
import ru.manager.ProgectManager.DTO.response.calendar.CalendarResponseList;
import ru.manager.ProgectManager.DTO.response.calendar.ShortKanbanElementInfo;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.repositories.ProjectRepository;
import ru.manager.ProgectManager.repositories.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public Optional<CalendarResponseList> findCalendar(long projectId, int year, int month, String userLogin) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Optional<UserWithProjectConnector> connector = user.getUserWithProjectConnectors()
                .stream()
                .filter(c -> c.getProject().equals(project))
                .findAny();
        if (connector.isPresent()) {
            int zoneId = user.getZoneId();
            Set<Kanban> availableKanbans = (connector.get().getRoleType() == TypeRoleProject.CUSTOM_ROLE
                    ? connector.get().getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                    .map(CustomRoleWithKanbanConnector::getKanban)
                    .collect(Collectors.toSet())
                    : project.getKanbans());
            Set<KanbanElement> elements = availableKanbans.stream()
                    .flatMap(kanban -> kanban.getKanbanColumns().stream())
                    .flatMap(kanbanColumn -> kanbanColumn.getElements().stream())
                    .filter(kanbanElement -> LocalDateTime
                            .ofEpochSecond(kanbanElement.getSelectedDate(), 0, ZoneOffset.ofHours(zoneId))
                            .getYear() == year)
                    .filter(kanbanElement ->
                            LocalDateTime
                                    .ofEpochSecond(kanbanElement.getSelectedDate(), 0, ZoneOffset.ofHours(zoneId))
                                    .getMonthValue() == month)
                    .collect(Collectors.toSet());
            Map<Long, Set<ShortKanbanElementInfo>> groups = new HashMap<>();
            for (KanbanElement element : elements) {
                if (!groups.containsKey(element.getSelectedDate())) {
                    groups.put(element.getSelectedDate(), new HashSet<>());
                }
                groups.get(element.getSelectedDate()).add(new ShortKanbanElementInfo(element));
            }
            return Optional.of(new CalendarResponseList(groups.entrySet().stream()
                    .map(entry -> new CalendarResponse(LocalDateTime
                            .ofEpochSecond(entry.getKey(), 0, ZoneOffset.ofHours(zoneId))
                            .toString(), entry.getValue()))
                    .collect(Collectors.toSet())));
        } else {
            return Optional.empty();
        }
    }
}
