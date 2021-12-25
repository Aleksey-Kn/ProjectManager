package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.ProjectRequestDTO;
import ru.manager.ProgectManager.entitys.*;
import ru.manager.ProgectManager.repositories.ProjectRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.UserWithProjectConnectorRepository;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserWithProjectConnectorRepository connectorRepository;

    public void addProject(ProjectRequestDTO requestDTO, String userLogin){
        User owner = userRepository.findByUsername(userLogin);

        Project project = new Project();
        project.setName(requestDTO.getName());

        UserWithProjectConnector connector = new UserWithProjectConnector();
        connector.setProject(project);
        connector.setUser(owner);
        connector.setAdmin(true);

        owner.getUserWithProjectConnectors().add(connector);
        userRepository.save(owner);

        project.setConnectors(Collections.singletonList(connector));
        projectRepository.save(project);

        connectorRepository.save(connector);
    }

    public List<KanbanColumn> findKanbans(long id){
        List<KanbanColumn> result = projectRepository.findById(id).get().getKanbanColumns();
        result.sort(Comparator.comparing(KanbanColumn::getSerialNumber));
        for(KanbanColumn column: result){
            column.getElements().sort(Comparator.comparing(KanbanElement::getSerialNumber));
        }
        return result;
    }
}
