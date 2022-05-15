package ru.manager.ProgectManager.services.project;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.ProjectDataRequest;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.repositories.ProjectRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.UserWithProjectConnectorRepository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserWithProjectConnectorRepository connectorRepository;

    public Optional<Project> findProject(long id, String login) {
        User user = userRepository.findByUsername(login);
        Project project = projectRepository.findById(id).orElseThrow();
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

    public boolean setPhoto(long id, byte[] photo, String userLogin) throws IOException {
        User admin = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(id).orElseThrow();
        if (isAdmin(project, admin)) {
            project.setPhoto(photo);
            projectRepository.save(project);
            return true;
        }
        return false;
    }

    public boolean setData(long id, ProjectDataRequest request, String userLogin) {
        Project project = projectRepository.findById(id).orElseThrow();
        User admin = userRepository.findByUsername(userLogin);
        if (isAdmin(project, admin)) {
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

    public boolean deleteProject(long id, String adminLogin) {
        User admin = userRepository.findByUsername(adminLogin);
        Project project = projectRepository.findById(id).orElseThrow();
        if (isAdmin(project, admin)) {
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

    public Set<User> findAllParticipants(long id){
        return projectRepository.findById(id).orElseThrow().getConnectors().stream()
                .map(UserWithProjectConnector::getUser)
                .collect(Collectors.toSet());
    }

    private boolean isAdmin(Project project, User admin){
       return project.getConnectors().stream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getUser().equals(admin));
    }
}
