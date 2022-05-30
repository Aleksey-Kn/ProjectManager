package ru.manager.ProgectManager.services.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.accessProject.AccessProjectTroughMailRequest;
import ru.manager.ProgectManager.DTO.response.project.ProjectResponse;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.AccessProject;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.IllegalActionException;
import ru.manager.ProgectManager.exception.NoSuchResourceException;
import ru.manager.ProgectManager.repositories.*;
import ru.manager.ProgectManager.services.MailService;
import ru.manager.ProgectManager.services.user.NotificationService;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Log
public class AccessProjectService {
    private final UserRepository userRepository;
    private final KanbanRepository kanbanRepository;
    private final AccessProjectRepository accessProjectRepository;
    private final ProjectRepository projectRepository;
    private final UserWithProjectConnectorRepository projectConnectorRepository;
    private final MailService mailService;
    private final NotificationService notificationService;
    private final ProjectService projectService;

    public Optional<AccessProject> generateTokenForAccessProject(String fromUser,
                                                                 long projectId,
                                                                 TypeRoleProject typeRoleProject,
                                                                 long customProjectRoleId,
                                                                 boolean disposable,
                                                                 int liveTime) {
        User user = userRepository.findByUsername(fromUser);
        Project project = projectRepository.findById(projectId).orElseThrow();
        if (typeRoleProject == TypeRoleProject.ADMIN)
            throw new IllegalArgumentException();
        if (isAdmin(project, user)) {
            return Optional.of(accessProjectRepository.save(createAccessProject(UUID.randomUUID().toString(), project,
                    typeRoleProject, customProjectRoleId, disposable, liveTime)));
        }
        return Optional.empty();
    }

    public boolean sendInvitationToMail(AccessProjectTroughMailRequest request, String adminLogin) {
        User admin = userRepository.findByUsername(adminLogin);
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow();
        if (isAdmin(project, admin)) {
            String token = UUID.randomUUID().toString();
            AccessProject accessProject = createAccessProject(token, project, request.getTypeRoleProject(),
                    request.getRoleId(), true, request.getLiveTimeInDays());
            User targetUser = userRepository.findByEmail(request.getEmail()).orElseThrow(IllegalArgumentException::new);
            mailService.sendInvitationToProject(targetUser, project.getName(), request.getUrl(), token);
            notificationService
                    .addNotificationAboutInvitationToProject(token, project.getName(), request.getUrl(), targetUser);
            accessProjectRepository.save(accessProject);
            return true;
        } else {
            return false;
        }
    }

    private AccessProject createAccessProject(String token, Project project, TypeRoleProject typeRoleProject,
                                              long customProjectRoleId, boolean disposable, int liveTime) {
        AccessProject accessProject = new AccessProject();
        accessProject.setProject(project);
        accessProject.setTypeRoleProject(typeRoleProject);
        if (typeRoleProject == TypeRoleProject.CUSTOM_ROLE) {
            accessProject.setProjectRole(project.getAvailableRole().stream()
                    .filter(r -> r.getId() == customProjectRoleId)
                    .findAny()
                    .orElseThrow(NoSuchResourceException::new));
        }
        accessProject.setDisposable(disposable);
        accessProject.setCode(token);
        accessProject.setTimeForDie(LocalDate.now().plusDays(liveTime).toEpochDay());
        return accessProject;
    }

    public Optional<ProjectResponse> findInfoOfProjectFromAccessToken(String token, int zoneId) {
        AccessProject accessProject = accessProjectRepository.findById(token).orElseThrow();
        if (LocalDate.ofEpochDay(accessProject.getTimeForDie()).isAfter(LocalDate.now())) {
            return Optional.of(new ProjectResponse(accessProject.getProject(),
                    (accessProject.getTypeRoleProject() == TypeRoleProject.CUSTOM_ROLE
                            ? accessProject.getProjectRole().getName()
                            : accessProject.getTypeRoleProject().name()), zoneId));
        } else {
            return Optional.empty();
        }
    }

    public boolean createAccessForUser(String token, String toUser) {
        AccessProject accessProject = accessProjectRepository.findById(token).orElseThrow();
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
                projectConnectorRepository.save(connector);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean leave(long projectId, String userLogin) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isPresent()) {
            if(project.get().getConnectors().size() == 1) {
                projectService.deleteProject(projectId, userLogin);
            } else {
                userRepository.findByUsername(userLogin).getUserWithProjectConnectors().parallelStream()
                        .filter(c -> c.getProject().equals(project.get()))
                        .findAny().ifPresent(connector -> {
                            if (connector.getRoleType() != TypeRoleProject.ADMIN || project.get().getConnectors().stream()
                                    .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN).count() > 1) {
                                removeConnector(connector, project.get());
                            } else throw new IllegalActionException();
                        });
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean kick(long projectId, long userId, String adminLogin) {
        User admin = userRepository.findByUsername(adminLogin);
        Project project = projectRepository.findById(projectId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow(NoSuchResourceException::new);
        if (isAdmin(project, admin) && !isAdmin(project, user)) {
            notificationService.addNotificationAboutDeleteFromProject(project.getName(), user);
            user.getUserWithProjectConnectors().parallelStream()
                    .filter(c -> c.getProject().equals(project))
                    .forEach(c -> removeConnector(c, project));
            return true;
        } else {
            return false;
        }
    }

    private void removeConnector(UserWithProjectConnector c, Project project) {
        c.getUser().getUserWithProjectConnectors().remove(c);
        userRepository.save(c.getUser());
        project.getConnectors().remove(c);
        projectRepository.save(project);
        projectConnectorRepository.delete(c);
    }

    public boolean canEditKanban(long id, String userLogin) {
        Kanban kanban = kanbanRepository.findById(id).orElseThrow();
        return userRepository.findByUsername(userLogin).getUserWithProjectConnectors().stream()
                .filter(c -> c.getProject().getKanbans().contains(kanban))
                .anyMatch(c -> (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                        || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                        .filter(CustomRoleWithKanbanConnector::isCanEdit)
                        .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))));
    }

    public String findUserRoleName(String userLogin, long projectId) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(projectId).orElseThrow();
        UserWithProjectConnector connector = project.getConnectors().stream()
                .filter(c -> c.getUser().equals(user))
                .findAny().orElseThrow();
        return (connector.getRoleType() == TypeRoleProject.CUSTOM_ROLE ? connector.getCustomProjectRole().getName() :
                connector.getRoleType().name());
    }

    private boolean isAdmin(Project project, User user) {
        return user.getUserWithProjectConnectors().stream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getProject().equals(project));
    }
}
