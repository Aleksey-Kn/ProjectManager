package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.entitys.AccessProject;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.entitys.UserWithProjectConnector;
import ru.manager.ProgectManager.repositories.AccessProjectRepository;
import ru.manager.ProgectManager.repositories.ProjectRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.UserWithProjectConnectorRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AccessService {
    private final UserRepository userRepository;
    private final AccessProjectRepository accessProjectRepository;
    private final ProjectRepository projectRepository;
    private final UserWithProjectConnectorRepository connectorRepository;

    public Optional<AccessProject> generateTokenForAccessProject(String fromUser,
                                                          long projectId,
                                                          boolean hasAdmin,
                                                          boolean disposable,
                                                          int liveTime) {
        User user = userRepository.findByUsername(fromUser);
        Project project = projectRepository.findById(projectId).get();
        if (hasAdmin && !disposable)
            throw new IllegalArgumentException();
        if (user.getUserWithProjectConnectors().stream()
                .filter(UserWithProjectConnector::isAdmin)
                .anyMatch(c -> c.getProject().equals(project))) {
            AccessProject accessProject = new AccessProject();
            accessProject.setProject(project);
            accessProject.setAdmin(hasAdmin);
            accessProject.setDisposable(disposable);
            accessProject.setCode(UUID.randomUUID().toString());
            accessProject.setTimeForDie(LocalDate.now().plusDays(liveTime).toEpochDay());
            return Optional.of(accessProjectRepository.save(accessProject));
        }
        return Optional.empty();
    }

    public boolean createAccessForUser(String token, String toUser) {
        AccessProject accessProject = accessProjectRepository.findById(token).get();
        if(accessProject.isDisposable() || LocalDate.ofEpochDay(accessProject.getTimeForDie()).isBefore(LocalDate.now())){
            accessProjectRepository.delete(accessProject);
        }
        if(LocalDate.ofEpochDay(accessProject.getTimeForDie()).isAfter(LocalDate.now())) {
            User user = userRepository.findByUsername(toUser);
            Project project = accessProject.getProject();
            if(user.getUserWithProjectConnectors().stream().noneMatch(c -> c.getProject().equals(project))
                    || user.getUserWithProjectConnectors().stream()
                    .filter(c -> c.getProject().equals(project))
                    .noneMatch(UserWithProjectConnector::isAdmin)
                    && accessProject.isAdmin()) {
                UserWithProjectConnector connector = new UserWithProjectConnector();
                connector.setUser(user);
                connector.setProject(project);
                connector.setAdmin(accessProject.isAdmin());
                connector = connectorRepository.save(connector);

                project.getConnectors().add(connector);
                user.getUserWithProjectConnectors().add(connector);
                projectRepository.save(project);
                userRepository.save(user);
            }
            return true;
        }
        return false;
    }
}
