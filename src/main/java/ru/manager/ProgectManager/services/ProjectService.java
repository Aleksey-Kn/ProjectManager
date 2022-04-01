package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.ProjectDataRequest;
import ru.manager.ProgectManager.entitys.Kanban;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.entitys.UserWithProjectConnector;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.repositories.KanbanRepository;
import ru.manager.ProgectManager.repositories.ProjectRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.UserWithProjectConnectorRepository;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserWithProjectConnectorRepository connectorRepository;
    private final KanbanRepository kanbanRepository;

    public Optional<Kanban> createKanban(long projectId, String name, String userLogin) {
        Project project = projectRepository.findById(projectId).get();
        if (userRepository.findByUsername(userLogin).getUserWithProjectConnectors()
                .stream().anyMatch(c -> c.getProject().equals(project))) {
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
                .stream().anyMatch(c -> c.getProject().equals(project))) {
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
        if (kanban.getProject().getConnectors().stream().anyMatch(p -> p.getUser().equals(user))) {
            return Optional.of(kanban);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Set<Kanban>> findAllKanban(long id, String userLogin) {
        Project project = projectRepository.findById(id).get();
        User user = userRepository.findByUsername(userLogin);
        if (project.getConnectors().stream().anyMatch(p -> p.getUser().equals(user))) {
            return Optional.of(project.getKanbans());
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
