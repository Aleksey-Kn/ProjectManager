package ru.manager.ProgectManager.services.kanban;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.kanban.TagRequest;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.kanban.Tag;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.repositories.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KanbanService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final KanbanRepository kanbanRepository;
    private final CustomProjectRoleRepository customProjectRoleRepository;
    private final KanbanConnectorRepository kanbanConnectorRepository;
    private final TagRepository tagRepository;

    public Optional<Kanban> createKanban(long projectId, String name, String userLogin) {
        Project project = projectRepository.findById(projectId).get();
        if (userRepository.findByUsername(userLogin).getUserWithProjectConnectors()
                .stream().anyMatch(c -> c.getProject().equals(project)
                        && (c.getRoleType() == TypeRoleProject.ADMIN || c.getCustomProjectRole().isCanEditResources()))) {
            Kanban kanban = new Kanban();
            kanban.setProject(project);
            kanban.setName(name);
            project.getKanbans().add(kanban);

            kanban = kanbanRepository.save(kanban);
            projectRepository.save(project);
            return Optional.of(kanban);
        }
        return Optional.empty();
    }

    public boolean removeKanban(long id, String userLogin) {
        Kanban kanban = kanbanRepository.findById(id).get();
        Project project = kanban.getProject();
        if (userRepository.findByUsername(userLogin).getUserWithProjectConnectors()
                .stream().anyMatch(c -> c.getProject().equals(project)
                        && (c.getRoleType() == TypeRoleProject.ADMIN || c.getCustomProjectRole().isCanEditResources()))) {
            project.getAvailableRole().forEach(r -> {
                Optional<CustomRoleWithKanbanConnector> removeConnector = r.getCustomRoleWithKanbanConnectors().stream()
                        .filter(c -> c.getKanban().getId() == id).findAny();
                if(removeConnector.isPresent()) {
                    r.getCustomRoleWithKanbanConnectors().remove(removeConnector.get());
                    kanbanConnectorRepository.delete(removeConnector.get());
                    customProjectRoleRepository.save(r);
                }
            });
            project.getKanbans().remove(kanban);
            projectRepository.save(project);
            return true;
        }
        return false;
    }

    public Optional<Kanban> findKanban(long id, String userLogin) {
        Kanban kanban = kanbanRepository.findById(id).get();
        User user = userRepository.findByUsername(userLogin);
        if (kanban.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().parallelStream()
                .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))))) {
            return Optional.of(kanban);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Set<Kanban>> findAllKanban(long id, String userLogin) {
        Project project = projectRepository.findById(id).get();
        User user = userRepository.findByUsername(userLogin);
        Optional<UserWithProjectConnector> connector = user.getUserWithProjectConnectors().stream()
                .filter(c -> c.getProject().equals(project))
                .findAny();
        if (connector.isPresent()) {
            if(connector.get().getRoleType() == TypeRoleProject.CUSTOM_ROLE) {
                return Optional.of(connector.get().getCustomProjectRole().getCustomRoleWithKanbanConnectors().parallelStream()
                        .map(CustomRoleWithKanbanConnector::getKanban)
                        .collect(Collectors.toSet()));
            } else{
                return Optional.of(project.getKanbans());
            }
        } else {
            return Optional.empty();
        }
    }

    public boolean addTag(long id, TagRequest request, String userLogin){
        Kanban kanban = kanbanRepository.findById(id).get();
        User user = userRepository.findByUsername(userLogin);
        if(kanban.getProject().getConnectors().parallelStream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().parallelStream()
                .filter(CustomRoleWithKanbanConnector::isCanEdit)
                .anyMatch(connector -> connector.getKanban().equals(kanban))))){
            Tag tag = new Tag();
            tag.setColor(request.getColor());
            tag.setText(request.getText());
            kanban.getAvailableTags().add(tagRepository.save(tag));
            kanbanRepository.save(kanban);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeTag(long kanbanId, long tagId, String userLogin){
        Kanban kanban = kanbanRepository.findById(kanbanId).get();
        User user = userRepository.findByUsername(userLogin);
        if(kanban.getProject().getConnectors().parallelStream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().parallelStream()
                .filter(CustomRoleWithKanbanConnector::isCanEdit)
                .anyMatch(customRoleWithKanbanConnector -> customRoleWithKanbanConnector.getKanban().equals(kanban))))){
            Tag tag = tagRepository.findById(tagId).orElseThrow(IllegalArgumentException::new);
            kanban.getAvailableTags().remove(tag);
            tagRepository.delete(tag);
            kanbanRepository.save(kanban);
            return true;
        } else{
            return false;
        }
    }
}
