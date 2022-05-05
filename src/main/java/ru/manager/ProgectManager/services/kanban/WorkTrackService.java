package ru.manager.ProgectManager.services.kanban;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.user.WorkTrackRequest;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.entitys.user.WorkTrack;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.repositories.KanbanElementRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.WorkTrackRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class WorkTrackService {
    private final WorkTrackRepository workTrackRepository;
    private final UserRepository userRepository;
    private final KanbanElementRepository elementRepository;

    public boolean addWorkTrack(WorkTrackRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(request.getTaskId()).orElseThrow();
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (kanban.getProject().getConnectors().parallelStream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                .filter(CustomRoleWithKanbanConnector::isCanEdit)
                .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))))) {
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
        if(workTrack.getOwner().equals(user)) {
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
}
