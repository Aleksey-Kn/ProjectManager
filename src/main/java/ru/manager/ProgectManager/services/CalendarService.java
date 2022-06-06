package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.response.calendar.CalendarResponse;
import ru.manager.ProgectManager.DTO.response.calendar.CalendarResponseList;
import ru.manager.ProgectManager.DTO.response.calendar.ShortKanbanElementInfo;
import ru.manager.ProgectManager.DTO.response.calendar.ShortKanbanElementInfoList;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.ElementStatus;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.repositories.KanbanRepository;
import ru.manager.ProgectManager.repositories.ProjectRepository;
import ru.manager.ProgectManager.repositories.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final KanbanRepository kanbanRepository;

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
                    .filter(element -> element.getStatus() == ElementStatus.ALIVE)
                    .filter(kanbanElement ->
                            belongThisYearAndMonths(kanbanElement.getSelectedDate(), year, month, zoneId))
                    .collect(Collectors.toSet());
            return groupByDay(elements, user);
        } else {
            return Optional.empty();
        }
    }

    public Optional<CalendarResponseList> findCalendarOnKanban(long id, int year, int month, String userLogin) {
        Kanban kanban = kanbanRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Project project = kanban.getProject();
        if (user.getUserWithProjectConnectors().parallelStream().anyMatch(connector ->
                connector.getProject().equals(project)
                        && (connector.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                        || connector.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                        .anyMatch(kc -> kc.getKanban().getId() == id)))) {
            int zoneId = user.getZoneId();
            Set<KanbanElement> elements = kanban.getKanbanColumns().stream()
                    .flatMap(kanbanColumn -> kanbanColumn.getElements().stream())
                    .filter(element -> element.getStatus() == ElementStatus.ALIVE)
                    .filter(kanbanElement ->
                            belongThisYearAndMonths(kanbanElement.getSelectedDate(), year, month, zoneId))
                    .collect(Collectors.toSet());
            return groupByDay(elements, user);
        } else {
            return Optional.empty();
        }
    }

    public ShortKanbanElementInfoList findTaskOnDay(String date, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        int zoneId = user.getZoneId();
        LocalDate targetDate = LocalDateTime.parse(date).toLocalDate();
        List<ShortKanbanElementInfo> kanbanElementInfos = user.getUserWithProjectConnectors().stream()
                .flatMap(connector -> connector.getRoleType() == TypeRoleProject.CUSTOM_ROLE
                        ? connector.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                        .map(CustomRoleWithKanbanConnector::getKanban)
                        : connector.getProject().getKanbans().stream())
                .flatMap(kanban -> kanban.getKanbanColumns().stream())
                .flatMap(kanbanColumn -> kanbanColumn.getElements().stream())
                .filter(element -> element.getStatus() == ElementStatus.ALIVE)
                .filter(element -> LocalDateTime.ofEpochSecond(element.getSelectedDate(), 0,
                        ZoneOffset.ofHours(zoneId)).toLocalDate().isEqual(targetDate))
                .map(element -> new ShortKanbanElementInfo(element, canEditElement(element, user)))
                .collect(Collectors.toList());
        return new ShortKanbanElementInfoList(kanbanElementInfos);
    }


    private boolean belongThisYearAndMonths(long epochSeconds, int year, int month, int zoneId) {
        LocalDateTime localDateTime = LocalDateTime
                .ofEpochSecond(epochSeconds, 0, ZoneOffset.ofHours(zoneId));
        return localDateTime.getYear() == year && localDateTime.getMonthValue() == month;
    }

    private Optional<CalendarResponseList> groupByDay(Set<KanbanElement> elements, User user) {
        int zoneId = user.getZoneId();
        Map<Long, Set<ShortKanbanElementInfo>> groups = new HashMap<>();
        for (KanbanElement element : elements) {
            if (!groups.containsKey(LocalDateTime.ofEpochSecond(element.getSelectedDate(), 0,
                    ZoneOffset.ofHours(zoneId)).toLocalDate().toEpochDay())) {
                groups.put(LocalDateTime.ofEpochSecond(element.getSelectedDate(), 0,
                        ZoneOffset.ofHours(zoneId)).toLocalDate().toEpochDay(), new HashSet<>());
            }
            groups.get(LocalDateTime.ofEpochSecond(element.getSelectedDate(), 0,
                    ZoneOffset.ofHours(zoneId)).toLocalDate().toEpochDay())
                    .add(new ShortKanbanElementInfo(element, canEditElement(element, user)));
        }
        return Optional.of(new CalendarResponseList(groups.entrySet().stream()
                .map(entry -> new CalendarResponse(LocalDate.ofEpochDay(entry.getKey()).toString(), entry.getValue()))
                .collect(Collectors.toSet())));
    }

    private boolean canEditElement(KanbanElement element, User user) {
        Kanban kanban = element.getKanbanColumn().getKanban();
        return kanban.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                .filter(CustomRoleWithKanbanConnector::isCanEdit)
                .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))));
    }
}
