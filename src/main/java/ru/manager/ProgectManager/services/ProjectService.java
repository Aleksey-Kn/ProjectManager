package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.manager.ProgectManager.DTO.request.NameRequestDTO;
import ru.manager.ProgectManager.entitys.*;
import ru.manager.ProgectManager.repositories.ProjectRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.UserWithProjectConnectorRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserWithProjectConnectorRepository connectorRepository;

    public Optional<Project> findProject(long id, String login){
        User user = userRepository.findByUsername(login);
        Project project = projectRepository.findById(id).get();
        if(project.getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
            return Optional.of(project);
        }
        return Optional.empty();
    }

    public Project addProject(NameRequestDTO requestDTO, String userLogin){
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

    public boolean setPhoto(long id, MultipartFile photo) throws IOException {
        Optional<Project> project = projectRepository.findById(id);
        if(project.isPresent()) {
            project.get().setPhoto(photo.getBytes());
            projectRepository.save(project.get());
            return true;
        }
        return false;
    }

    public boolean setName(long id, NameRequestDTO name){
        Optional<Project> project = projectRepository.findById(id);
        if(project.isPresent()) {
            project.get().setName(name.getName());
            projectRepository.save(project.get());
            return true;
        }
        return false;
    }

    public boolean deleteProject(long id, String adminLogin){
        User admin = userRepository.findByUsername(adminLogin);
        assert admin != null;
        Project project = projectRepository.findById(id).get();
        if(project.getConnectors().stream()
                .filter(UserWithProjectConnector::isAdmin)
                .anyMatch(c -> c.getUser().equals(admin))){
            List<UserWithProjectConnector> removable = project.getConnectors();
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

    public Optional<List<KanbanColumn>> findKanbans(long id, String userLogin){
        if(userRepository.findByUsername(userLogin).getUserWithProjectConnectors().stream()
                .map(UserWithProjectConnector::getProject)
                .anyMatch(p -> p.getId() == id)) {
            List<KanbanColumn> result = projectRepository.findById(id).get().getKanbanColumns();
            result.sort(Comparator.comparing(KanbanColumn::getSerialNumber));
            for (KanbanColumn column : result) {
                column.getElements().sort(Comparator.comparing(KanbanElement::getSerialNumber));
            }
            return Optional.of(result);
        }
        return Optional.empty();
    }
}
