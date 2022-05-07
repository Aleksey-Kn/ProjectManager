package ru.manager.ProgectManager.services.kanban;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import ru.manager.ProgectManager.exception.IncorrectStatusException;
import ru.manager.ProgectManager.repositories.KanbanElementRepository;
import ru.manager.ProgectManager.repositories.ProjectRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.WorkTrackRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkTrackService {
    private final WorkTrackRepository workTrackRepository;
    private final UserRepository userRepository;
    private final KanbanElementRepository elementRepository;
    private final ProjectRepository projectRepository;

    public boolean addWorkTrack(CreateWorkTrackRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(request.getTaskId()).orElseThrow();
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
            elementRepository.save(element);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeWorkTrack(long trackId, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        WorkTrack workTrack = workTrackRepository.findById(trackId).orElseThrow();
        if (workTrack.getOwner().equals(user)) {
            checkElement(workTrack.getTask());
            user.getWorkTrackSet().remove(workTrack);
            userRepository.save(user);
            workTrack.getTask().getWorkTrackSet().remove(workTrack);
            elementRepository.save(workTrack.getTask());
            workTrackRepository.delete(workTrack);
            return true;
        } else {
            return false;
        }
    }

    public Optional<AllWorkUserInfo> findWorkTrackMyself(String from, String to, long projectId, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(projectId).orElseThrow();
        if(project.getConnectors().parallelStream().anyMatch(c -> c.getUser().equals(user))) {
            return Optional.of(findWorkTrack(LocalDate.parse(from), LocalDate.parse(to), project, user));
        } else {
            return Optional.empty();
        }
    }

    public Optional<AllWorkUserInfo> findOtherWorkTrackAsAdmin(String from, String to, long projectId, long targetUserId,
                                                               String adminLogin) {
        User admin = userRepository.findByUsername(adminLogin);
        Project project = projectRepository.findById(projectId).orElseThrow();
        if(project.getConnectors().parallelStream().anyMatch(c -> c.getUser().equals(admin))) {
            return Optional.of(findWorkTrack(LocalDate.parse(from), LocalDate.parse(to), project,
                    userRepository.findById(targetUserId).orElseThrow(IllegalArgumentException::new)));
        } else {
            return Optional.empty();
        }
    }

    private AllWorkUserInfo findWorkTrack(LocalDate fromDate, LocalDate toDate, Project project, User user) {
        AllWorkUserInfo info = new AllWorkUserInfo();

        info.setTasks(user.getWorkTrackSet().parallelStream()
                .map(WorkTrack::getTask)
                .filter(kanbanElement -> kanbanElement.getKanbanColumn().getKanban().getProject().equals(project))
                .map(element -> new ElementWithWorkResponse(element, user, fromDate, toDate))
                .collect(Collectors.toSet()));

        Map<Long, Integer> dateTime = new HashMap<>();
        user.getWorkTrackSet().parallelStream()
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

        info.setSummaryWorkInDiapason(user.getWorkTrackSet().parallelStream()
                .filter(workTrack -> workTrack.getTask().getKanbanColumn().getKanban().getProject().equals(project))
                .filter(workTrack -> LocalDate.ofEpochDay(workTrack.getWorkDate()).isAfter(fromDate))
                .filter(workTrack -> LocalDate.ofEpochDay(workTrack.getWorkDate()).isBefore(toDate))
                .mapToInt(WorkTrack::getWorkTime)
                .sum());

        return info;
    }

    private void checkElement(KanbanElement element) {
        if (element.getStatus() == ElementStatus.UTILISE)
            throw new IncorrectStatusException();
        if (element.getStatus() == ElementStatus.DELETED)
            throw new NoSuchElementException();
    }
}
