package ru.manager.ProgectManager.services.kanban;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.kanban.TagRequest;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.kanban.Tag;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.ResourceType;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.repositories.*;
import ru.manager.ProgectManager.services.user.VisitMarkUpdater;

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
    private final VisitMarkUpdater visitMarkUpdater;

    public Optional<Kanban> createKanban(long projectId, String name, String userLogin) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        if (canEditResource(project, userRepository.findByUsername(userLogin))) {
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
        Kanban kanban = kanbanRepository.findById(id).orElseThrow();
        Project project = kanban.getProject();
        User user = userRepository.findByUsername(userLogin);
        if (canEditResource(project, user) && canEditKanban(kanban, user)) {
            project.getAvailableRole().forEach(role -> role.getCustomRoleWithKanbanConnectors().stream()
                    .filter(kanbanConnector -> kanbanConnector.getKanban().getId() == id)
                    .forEach(kanbanConnector -> {
                        role.getCustomRoleWithKanbanConnectors().remove(kanbanConnector);
                        kanbanConnectorRepository.delete(kanbanConnector);
                        customProjectRoleRepository.save(role);
                    }));
            project.getKanbans().remove(kanban);
            projectRepository.save(project);
            return true;
        }
        return false;
    }

    public Optional<Kanban> findKanban(long id, String userLogin) {
        Kanban kanban = kanbanRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        if (canSeeKanban(kanban, user)) {
            visitMarkUpdater.updateVisitMarks(user, id, kanban.getName(), ResourceType.KANBAN);
            return Optional.of(kanban);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Set<Kanban>> findAllKanban(long id, String userLogin) {
        Project project = projectRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Optional<UserWithProjectConnector> connector = user.getUserWithProjectConnectors().stream()
                .filter(c -> c.getProject().equals(project))
                .findAny();
        if (connector.isPresent()) {
            if (connector.get().getRoleType() == TypeRoleProject.CUSTOM_ROLE) {
                return Optional.of(connector.get().getCustomProjectRole().getCustomRoleWithKanbanConnectors().parallelStream()
                        .map(CustomRoleWithKanbanConnector::getKanban)
                        .collect(Collectors.toSet()));
            } else {
                return Optional.of(project.getKanbans());
            }
        } else {
            return Optional.empty();
        }
    }

    public Optional<Set<Kanban>> findKanbansByName(long id, String inputName, String userLogin) {
        String name = inputName.toLowerCase();
        Project project = projectRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Optional<UserWithProjectConnector> connector = user.getUserWithProjectConnectors().stream()
                .filter(c -> c.getProject().equals(project))
                .findAny();
        if (connector.isPresent()) {
            if (connector.get().getRoleType() == TypeRoleProject.CUSTOM_ROLE) {
                return Optional.of(connector.get().getCustomProjectRole().getCustomRoleWithKanbanConnectors().parallelStream()
                        .map(CustomRoleWithKanbanConnector::getKanban)
                        .filter(k -> k.getName().toLowerCase().contains(name))
                        .collect(Collectors.toSet()));
            } else {
                return Optional.of(project.getKanbans().stream()
                        .filter(k -> k.getName().toLowerCase().contains(name))
                        .collect(Collectors.toSet()));
            }
        } else {
            return Optional.empty();
        }
    }

    public Optional<Tag> addTag(long id, TagRequest request, String userLogin) {
        Kanban kanban = kanbanRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        if (canEditKanban(kanban, user)) {
            Tag tag = new Tag();
            tag.setColor(request.getColor());
            tag.setText(request.getText());
            tag.setKanban(kanban);
            tag = tagRepository.save(tag);
            kanban.getAvailableTags().add(tag);
            kanbanRepository.save(kanban);
            return Optional.of(tag);
        } else {
            return Optional.empty();
        }
    }

    public boolean removeTag(long id, String userLogin) {
        Tag tag = tagRepository.findById(id).orElseThrow();
        Kanban kanban = tag.getKanban();
        User user = userRepository.findByUsername(userLogin);
        if (canEditKanban(kanban, user)) {
            kanban.getAvailableTags().remove(tag);
            kanban.getKanbanColumns().stream().flatMap(c -> c.getElements().stream())
                    .filter(e -> e.getTags().contains(tag))
                    .forEach(e -> e.getTags().remove(tag));
            tagRepository.delete(tag);
            kanbanRepository.save(kanban);
            return true;
        } else {
            return false;
        }
    }

    public boolean editTag(long id, TagRequest request, String userLogin) {
        Tag tag = tagRepository.findById(id).orElseThrow();
        Kanban kanban = tag.getKanban();
        User user = userRepository.findByUsername(userLogin);
        if (canEditKanban(kanban, user)) {
            tag.setText(request.getText());
            tag.setColor(request.getColor());
            tagRepository.save(tag);
            return true;
        } else {
            return false;
        }
    }

    public Optional<Set<Tag>> findAllAvailableTags(long kanbanId, String userLogin) {
        Kanban kanban = kanbanRepository.findById(kanbanId).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        if (canSeeKanban(kanban, user)) {
            return Optional.of(kanban.getAvailableTags());
        } else {
            return Optional.empty();
        }
    }

    private boolean canEditResource(Project project, User user) {
        return user.getUserWithProjectConnectors()
                .stream().anyMatch(c -> c.getProject().equals(project)
                        && (c.getRoleType() == TypeRoleProject.ADMIN || (c.getRoleType() == TypeRoleProject.CUSTOM_ROLE
                        && c.getCustomProjectRole().isCanEditResources())));
    }

    private boolean canEditKanban(Kanban kanban, User user) {
        return kanban.getProject().getConnectors().parallelStream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().parallelStream()
                .filter(CustomRoleWithKanbanConnector::isCanEdit)
                .anyMatch(connector -> connector.getKanban().equals(kanban))));
    }

    private boolean canSeeKanban(Kanban kanban, User user) {
        return kanban.getProject().getConnectors().parallelStream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().parallelStream()
                .anyMatch(customRoleWithKanbanConnector -> customRoleWithKanbanConnector.getKanban().equals(kanban))));
    }
}
