package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.CreateCustomRoleRequest;
import ru.manager.ProgectManager.entitys.*;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.NoSuchResourceException;
import ru.manager.ProgectManager.repositories.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AccessProjectService {
    private final UserRepository userRepository;
    private final KanbanRepository kanbanRepository;
    private final AccessProjectRepository accessProjectRepository;
    private final ProjectRepository projectRepository;
    private final UserWithProjectConnectorRepository connectorRepository;
    private final CustomProjectRoleRepository customProjectRoleRepository;
    private final KanbanConnectorRepository kanbanConnectorRepository;

    public boolean createCustomRole(long projectId, CreateCustomRoleRequest request, String userLogin){
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(projectId).get();
        if(user.getUserWithProjectConnectors().stream().filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getProject().equals(project))) {
            CustomProjectRole customProjectRole = new CustomProjectRole();
            customProjectRole.setName(request.getName());
            customProjectRole.setCanEditResources(request.isCanEditResource());
            customProjectRole.setKanbanConnectors(request.getKanbanConnectorRequests().stream().map(kr -> {
                KanbanConnector kanbanConnector = new KanbanConnector();
                kanbanConnector.setCanEdit(kr.isCanEdit());
                kanbanConnector.setKanban(kanbanRepository.findById(kr.getId())
                        .orElseThrow(() -> new NoSuchResourceException(Long.toString(kr.getId()))));
                return kanbanConnectorRepository.save(kanbanConnector);
            }).collect(Collectors.toSet()));
            return true;
        }
        return false;
    }

    public Optional<AccessProject> generateTokenForAccessProject(String fromUser,
                                                          long projectId,
                                                          TypeRoleProject typeRoleProject,
                                                          String customProjectRoleName,
                                                          boolean disposable,
                                                          int liveTime) {
        User user = userRepository.findByUsername(fromUser);
        Project project = projectRepository.findById(projectId).get();
        if (typeRoleProject == TypeRoleProject.ADMIN && !disposable)
            throw new IllegalArgumentException();
        if (user.getUserWithProjectConnectors().stream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getProject().equals(project))) {
            AccessProject accessProject = new AccessProject();
            accessProject.setProject(project);
            accessProject.setTypeRoleProject(typeRoleProject);
            if(typeRoleProject == TypeRoleProject.CUSTOM_ROLE) {
                accessProject.setProjectRole(customProjectRoleRepository.findByName(customProjectRoleName)
                        .orElseThrow(() -> new NoSuchResourceException(customProjectRoleName)));
            }
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
                    .noneMatch(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                    && accessProject.getTypeRoleProject() == TypeRoleProject.ADMIN) {
                UserWithProjectConnector connector = new UserWithProjectConnector();
                connector.setUser(user);
                connector.setProject(project);
                connector.setRoleType(accessProject.getTypeRoleProject());
                connector.setCustomProjectRole(accessProject.getProjectRole());
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
