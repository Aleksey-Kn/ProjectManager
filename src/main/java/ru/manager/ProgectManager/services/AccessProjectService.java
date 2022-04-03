package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.CreateCustomRoleRequest;
import ru.manager.ProgectManager.DTO.request.EditUserRoleRequest;
import ru.manager.ProgectManager.entitys.*;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.NoSuchResourceException;
import ru.manager.ProgectManager.repositories.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
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
    private final KanbanConnectorRepository kanbanConnectorRepository;
    private final CustomProjectRoleRepository customProjectRoleRepository;

    public boolean createCustomRole(CreateCustomRoleRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(request.getProjectId()).get();
        if (user.getUserWithProjectConnectors().stream().filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getProject().equals(project))) {
            CustomProjectRole customProjectRole = new CustomProjectRole();
            setCustomProjectRoleData(project, customProjectRole, request);
            project.getAvailableRole().add(customProjectRole);
            projectRepository.save(project);
            return true;
        }
        return false;
    }

    public Optional<Set<CustomProjectRole>> findAllCustomProjectRole(long projectId, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(projectId).get();
        if (user.getUserWithProjectConnectors().stream().filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getProject().equals(project))) {
            return Optional.of(project.getAvailableRole());
        } else {
            return Optional.empty();
        }
    }

    public boolean deleteCustomRole(long projectId, long roleId, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(projectId).get();
        if (user.getUserWithProjectConnectors().stream().filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getProject().equals(project))) {
            CustomProjectRole role = project.getAvailableRole().stream()
                    .filter(r -> r.getId() == roleId)
                    .findAny().orElseThrow(IllegalArgumentException::new);
            project.getConnectors().parallelStream()
                    .filter(c -> c.getRoleType() == TypeRoleProject.CUSTOM_ROLE)
                    .filter(c -> c.getCustomProjectRole().equals(role))
                    .forEach(c -> {
                        c.setRoleType(TypeRoleProject.STANDARD_USER);
                        c.setCustomProjectRole(null);
                    });
            project.getAvailableRole().remove(role);
            projectRepository.save(project);
            return true;
        } else {
            return false;
        }
    }

    public boolean changeRole(long roleId, CreateCustomRoleRequest newCustomRole, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(newCustomRole.getProjectId()).get();
        if (user.getUserWithProjectConnectors().stream().filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getProject().equals(project))) {
            CustomProjectRole customProjectRole = project.getAvailableRole().stream()
                    .filter(r -> r.getId() == roleId)
                    .findAny().orElseThrow(IllegalArgumentException::new);
            customProjectRole.getKanbanConnectors().clear();
            setCustomProjectRoleData(project, customProjectRole, newCustomRole);
            customProjectRoleRepository.save(customProjectRole);
            return true;
        } else {
            return false;
        }
    }

    private void setCustomProjectRoleData(Project project, CustomProjectRole customProjectRole, CreateCustomRoleRequest request) {
        customProjectRole.setName(request.getName());
        customProjectRole.setCanEditResources(request.isCanEditResource());
        customProjectRole.setKanbanConnectors(request.getKanbanConnectorRequests().stream().map(kr -> {
            KanbanConnector kanbanConnector = new KanbanConnector();
            kanbanConnector.setCanEdit(kr.isCanEdit());
            kanbanConnector.setKanban(project.getKanbans().stream().filter(k -> k.getId() == kr.getId()).findAny()
                    .orElseThrow(() -> new NoSuchResourceException("Kanban: " + kr.getId())));
            return kanbanConnectorRepository.save(kanbanConnector);
        }).collect(Collectors.toSet()));
    }

    public boolean editUserRole(EditUserRoleRequest request, String adminLogin) {
        User admin = userRepository.findByUsername(adminLogin);
        Project project = projectRepository.findById(request.getProjectId()).get();
        if (admin.getUserWithProjectConnectors().stream().filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getProject().equals(project))) {
            User targetUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new NoSuchResourceException("User: " + request.getUserId()));
            UserWithProjectConnector connector = targetUser.getUserWithProjectConnectors().stream()
                    .filter(c -> c.getProject().equals(project))
                    .findAny()
                    .orElseThrow(() -> new NoSuchResourceException("Project connect with user: " + request.getUserId()));
            connector.setRoleType(request.getTypeRoleProject());
            if (request.getTypeRoleProject() == TypeRoleProject.CUSTOM_ROLE) {
                connector.setCustomProjectRole(project.getAvailableRole().stream()
                        .filter(r -> r.getId() == request.getCustomRoleId())
                        .findAny().orElseThrow(IllegalArgumentException::new));
            } else {
                connector.setCustomProjectRole(null);
            }
            return true;
        }
        return false;
    }

    public Optional<AccessProject> generateTokenForAccessProject(String fromUser,
                                                                 long projectId,
                                                                 TypeRoleProject typeRoleProject,
                                                                 long customProjectRoleId,
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
            if (typeRoleProject == TypeRoleProject.CUSTOM_ROLE) {
                accessProject.setProjectRole(project.getAvailableRole().stream()
                        .filter(r -> r.getId() == customProjectRoleId)
                        .findAny()
                        .orElseThrow(() -> new NoSuchResourceException("Custom project role: " + customProjectRoleId)));
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
        if (accessProject.isDisposable() || LocalDate.ofEpochDay(accessProject.getTimeForDie()).isBefore(LocalDate.now())) {
            accessProjectRepository.delete(accessProject);
        }
        if (LocalDate.ofEpochDay(accessProject.getTimeForDie()).isAfter(LocalDate.now())) {
            User user = userRepository.findByUsername(toUser);
            Project project = accessProject.getProject();
            if (user.getUserWithProjectConnectors().stream().noneMatch(c -> c.getProject().equals(project))) {
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

    public boolean canEditKanban(long id, String userLogin) {
        Kanban kanban = kanbanRepository.findById(id).get();
        return userRepository.findByUsername(userLogin).getUserWithProjectConnectors().stream()
                .filter(c -> c.getProject().getKanbans().contains(kanban))
                .anyMatch(c -> (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                                || c.getCustomProjectRole().getKanbanConnectors().stream()
                                .filter(KanbanConnector::isCanEdit)
                                .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))));
    }
}
