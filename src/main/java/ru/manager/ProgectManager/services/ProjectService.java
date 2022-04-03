package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.ProjectDataRequest;
import ru.manager.ProgectManager.entitys.*;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.repositories.*;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserWithProjectConnectorRepository connectorRepository;
    private final KanbanRepository kanbanRepository;
    private final CustomProjectRoleRepository customProjectRoleRepository;
    private final KanbanConnectorRepository kanbanConnectorRepository;

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

    public Optional<Project> findProject(long id, String login) {
        User user = userRepository.findByUsername(login);
        Project project = projectRepository.findById(id).get();
        if (project.getConnectors().stream().anyMatch(c -> c.getUser().equals(user))) {
            return Optional.of(project);
        }
        return Optional.empty();
    }

    public Project addProject(ProjectDataRequest request, String userLogin) {
        User owner = userRepository.findByUsername(userLogin);

        Project project = new Project();
        project.setName(request.getName());
        project.setStatus(request.getStatus());
        project.setStatusColor(request.getStatusColor());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setDeadline(request.getDeadline());

        UserWithProjectConnector connector = new UserWithProjectConnector();
        connector.setRoleType(TypeRoleProject.ADMIN);
        connector = connectorRepository.save(connector);

        project.setConnectors(Collections.singleton(connector));
        project = projectRepository.save(project);

        owner.getUserWithProjectConnectors().add(connector);
        userRepository.save(owner);

        connector.setProject(project);
        connector.setUser(owner);
        connectorRepository.save(connector);
        return project;
    }

    public boolean setPhoto(long id, byte[] photo, String userLogin, String filename) throws IOException {
        User admin = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(id).get();
        if (project.getConnectors().stream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getUser().equals(admin))) {
            project.setPhoto(photo);
            project.setDatatypePhoto(new MimetypesFileTypeMap().getContentType(filename));
            projectRepository.save(project);
            return true;
        }
        return false;
    }

    public boolean setData(long id, ProjectDataRequest request, String userLogin) {
        Project project = projectRepository.findById(id).get();
        User admin = userRepository.findByUsername(userLogin);
        if (project.getConnectors().stream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getUser().equals(admin))) {
            project.setName(request.getName());
            project.setStatus(request.getStatus());
            project.setStatusColor(request.getStatusColor());
            project.setDescription(request.getDescription());
            project.setStartDate(request.getStartDate());
            project.setDeadline(request.getDeadline());
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

    public boolean deleteProject(long id, String adminLogin) {
        User admin = userRepository.findByUsername(adminLogin);
        Project project = projectRepository.findById(id).get();
        if (project.getConnectors().stream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getUser().equals(admin))) {
            List<UserWithProjectConnector> removable = new LinkedList<>(project.getConnectors());
            project.getConnectors().clear();
            removable.stream()
                    .map(UserWithProjectConnector::getUser)
                    .forEach(u -> {
                        u.getUserWithProjectConnectors().removeIf(c -> c.getProject().equals(project));
                        userRepository.save(u);
                    });
            connectorRepository.deleteAll(removable);
            projectRepository.delete(project);
            return true;
        }
        return false;
    }
}
