package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.NameRequest;
import ru.manager.ProgectManager.entitys.*;
import ru.manager.ProgectManager.repositories.KanbanColumnRepository;
import ru.manager.ProgectManager.repositories.ProjectRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.UserWithProjectConnectorRepository;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserWithProjectConnectorRepository connectorRepository;
    private final KanbanColumnRepository kanbanColumnRepository;

    public Optional<Project> findProject(long id, String login){
        User user = userRepository.findByUsername(login);
        Project project = projectRepository.findById(id).get();
        if(project.getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
            return Optional.of(project);
        }
        return Optional.empty();
    }

    public Project addProject(NameRequest requestDTO, String userLogin){
        User owner = userRepository.findByUsername(userLogin);

        Project project = new Project();
        project.setName(requestDTO.getName());

        UserWithProjectConnector connector = new UserWithProjectConnector();
        connector.setAdmin(true);
        connector = connectorRepository.save(connector);

        project.setConnectors(Collections.singletonList(connector));
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
        Project project = projectRepository.findById(id).get();
        if(project.getConnectors().stream()
                .filter(UserWithProjectConnector::isAdmin)
                .anyMatch(c -> c.getUser().equals(admin))) {
            project.setPhoto(photo);
            projectRepository.save(project);
            return true;
        }
        return false;
    }

    public boolean setName(long id, NameRequest name, String userLogin){
        Project project = projectRepository.findById(id).get();
        User admin = userRepository.findByUsername(userLogin);
        if(project.getConnectors().stream()
                .filter(UserWithProjectConnector::isAdmin)
                .anyMatch(c -> c.getUser().equals(admin))) {
            project.setName(name.getName());
            projectRepository.save(project);
            return true;
        }
        return false;
    }

    public boolean deleteProject(long id, String adminLogin){
        User admin = userRepository.findByUsername(adminLogin);
        Project project = projectRepository.findById(id).get();
        if(project.getConnectors().stream()
                .filter(UserWithProjectConnector::isAdmin)
                .anyMatch(c -> c.getUser().equals(admin))){
            List<UserWithProjectConnector> removable = new LinkedList<>(project.getConnectors());
            project.getConnectors().clear();
            removable.stream()
                    .map(UserWithProjectConnector::getUser)
                    .forEach(u -> u.getUserWithProjectConnectors().removeIf(c -> c.getProject().equals(project)));
            connectorRepository.deleteAll(removable);
            projectRepository.delete(project);
            return true;
        }
        return false;
    }
}
