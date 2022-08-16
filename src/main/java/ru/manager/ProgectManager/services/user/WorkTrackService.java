package ru.manager.ProgectManager.services.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.manager.ProgectManager.DTO.request.user.CreateWorkTrackRequest;
import ru.manager.ProgectManager.DTO.response.workTrack.AllWorkUserInfo;
import ru.manager.ProgectManager.DTO.response.workTrack.ElementWithWorkResponse;
import ru.manager.ProgectManager.DTO.response.workTrack.WorkTrackShortResponse;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.entitys.user.WorkTrack;
import ru.manager.ProgectManager.enums.ElementStatus;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.kanban.IncorrectElementStatusException;
import ru.manager.ProgectManager.exception.kanban.NoSuchKanbanElementException;
import ru.manager.ProgectManager.exception.project.NoSuchProjectException;
import ru.manager.ProgectManager.exception.user.NoSuchUserException;
import ru.manager.ProgectManager.repositories.KanbanElementRepository;
import ru.manager.ProgectManager.repositories.ProjectRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.WorkTrackRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class WorkTrackService {
    private final WorkTrackRepository workTrackRepository;
    private final UserRepository userRepository;
    private final KanbanElementRepository elementRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public void addWorkTrack(CreateWorkTrackRequest request, String userLogin)
            throws NoSuchKanbanElementException, IncorrectElementStatusException, ForbiddenException {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(request.getTaskId())
                .orElseThrow(NoSuchKanbanElementException::new);
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (kanban.getProject().getConnectors().parallelStream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                .filter(CustomRoleWithKanbanConnector::isCanEdit)
                .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))))) {
            checkElement(element);
            WorkTrack workTrack = new WorkTrack();
            workTrack.setWorkDate(LocalDate.now().toEpochDay());
            workTrack.setWorkTime(request.getWorkTime());
            workTrack.setOwner(user);
            workTrack.setTask(element);
            workTrack.setComment(request.getComment());
            workTrack = workTrackRepository.save(workTrack);

            user.getWorkTrackSet().add(workTrack);
            userRepository.save(user);

            element.getWorkTrackSet().add(workTrack);
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);
            elementRepository.save(element);
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public boolean removeWorkTrack(long trackId, String userLogin)
            throws IncorrectElementStatusException, NoSuchKanbanElementException, ForbiddenException {
        User user = userRepository.findByUsername(userLogin);
        Optional<WorkTrack> workTrack = workTrackRepository.findById(trackId);
        if (workTrack.isPresent()) {
            if (workTrack.get().getOwner().equals(user)) {
                checkElement(workTrack.get().getTask());
                workTrack.get().getTask().setTimeOfUpdate(getEpochSeconds());
                workTrack.get().getTask().setLastRedactor(user);

                user.getWorkTrackSet().remove(workTrack.get());
                userRepository.save(user);
                workTrack.get().getTask().getWorkTrackSet().remove(workTrack.get());
                elementRepository.save(workTrack.get().getTask());
                workTrackRepository.delete(workTrack.get());
                return true;
            } else {
                throw new ForbiddenException();
            }
        } else return false;
    }

    public AllWorkUserInfo findWorkTrackMyself(String from, String to, long projectId, String userLogin)
            throws ForbiddenException, NoSuchProjectException {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(projectId).orElseThrow(NoSuchProjectException::new);
        if (project.getConnectors().parallelStream().anyMatch(c -> c.getUser().equals(user))) {
            return findWorkTrack(LocalDate.parse(from), LocalDate.parse(to), project, user);
        } else {
            throw new ForbiddenException();
        }
    }

    public AllWorkUserInfo findOtherWorkTrackAsAdmin(String from, String to, long projectId, long targetUserId,
                                                               String adminLogin)
            throws NoSuchProjectException, ForbiddenException, NoSuchUserException {
        User admin = userRepository.findByUsername(adminLogin);
        Project project = projectRepository.findById(projectId).orElseThrow(NoSuchProjectException::new);
        if (project.getConnectors().parallelStream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getUser().equals(admin))) {
            return findWorkTrack(LocalDate.parse(from), LocalDate.parse(to), project,
                    userRepository.findById(targetUserId).orElseThrow(NoSuchUserException::new));
        } else {
            throw new ForbiddenException();
        }
    }

    private AllWorkUserInfo findWorkTrack(LocalDate fromDate, LocalDate toDate, Project project, User user) {
        AllWorkUserInfo info = new AllWorkUserInfo();

        info.setTasks(user.getWorkTrackSet().stream()
                .map(WorkTrack::getTask)
                .filter(kanbanElement -> kanbanElement.getKanbanColumn().getKanban().getProject().equals(project))
                .map(element -> new ElementWithWorkResponse(element, user, fromDate, toDate))
                .collect(Collectors.toSet()));

        Map<Long, Integer> dateTime = new HashMap<>();
        user.getWorkTrackSet().stream()
                .filter(workTrack -> workTrack.getTask().getKanbanColumn().getKanban().getProject().equals(project))
                .filter(workTrack -> LocalDate.ofEpochDay(workTrack.getWorkDate()).isAfter(fromDate))
                .filter(workTrack -> LocalDate.ofEpochDay(workTrack.getWorkDate()).isBefore(toDate))
                .forEach(workTrack -> {
                    if (dateTime.containsKey(workTrack.getWorkDate())) {
                        dateTime.put(workTrack.getWorkDate(),
                                dateTime.get(workTrack.getWorkDate()) + workTrack.getWorkTime());
                    } else {
                        dateTime.put(workTrack.getWorkDate(), workTrack.getWorkTime());
                    }
                });
        info.setWorkInConcreteDay(dateTime.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new WorkTrackShortResponse(LocalDate.ofEpochDay(e.getKey()).toString(), e.getValue()))
                .collect(Collectors.toList()));

        info.setSummaryWorkInDiapason(user.getWorkTrackSet().stream()
                .filter(workTrack -> workTrack.getTask().getKanbanColumn().getKanban().getProject().equals(project))
                .filter(workTrack -> LocalDate.ofEpochDay(workTrack.getWorkDate()).isAfter(fromDate))
                .filter(workTrack -> LocalDate.ofEpochDay(workTrack.getWorkDate()).isBefore(toDate))
                .mapToInt(WorkTrack::getWorkTime)
                .sum());

        return info;
    }

    private void checkElement(KanbanElement element)
            throws NoSuchKanbanElementException, IncorrectElementStatusException {
        if (element.getStatus() == ElementStatus.UTILISE)
            throw new IncorrectElementStatusException();
        if (element.getStatus() == ElementStatus.DELETED)
            throw new NoSuchKanbanElementException();
    }

    private long getEpochSeconds() {
        return LocalDateTime.now().toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
    }
}
