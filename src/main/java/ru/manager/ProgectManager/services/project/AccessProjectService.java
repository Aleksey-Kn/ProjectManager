package ru.manager.ProgectManager.services.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.manager.ProgectManager.DTO.request.accessProject.AccessProjectTroughMailRequest;
import ru.manager.ProgectManager.DTO.response.accessProject.AccessProjectResponse;
import ru.manager.ProgectManager.DTO.response.project.ProjectResponse;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.AccessProject;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.Locale;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.project.AccessTokenInvalidException;
import ru.manager.ProgectManager.exception.project.NoSuchCustomRoleException;
import ru.manager.ProgectManager.exception.project.NoSuchProjectException;
import ru.manager.ProgectManager.exception.runtime.IllegalActionException;
import ru.manager.ProgectManager.exception.user.NoSuchUserException;
import ru.manager.ProgectManager.repositories.*;
import ru.manager.ProgectManager.services.MailService;
import ru.manager.ProgectManager.services.user.NotificationService;
import ru.manager.ProgectManager.services.user.VisitMarkUpdater;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Transactional(readOnly = true)
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
    private final VisitMarkUpdater visitMarkUpdater;

    @Transactional
    public Optional<AccessProjectResponse> generateTokenForAccessProject(String fromUser,
                                                                         long projectId,
                                                                         TypeRoleProject typeRoleProject,
                                                                         long customProjectRoleId,
                                                                         boolean disposable,
                                                                         int liveTime) throws ForbiddenException, NoSuchProjectException, NoSuchCustomRoleException {
        User user = userRepository.findByUsername(fromUser);
        Project project = projectRepository.findById(projectId).orElseThrow(NoSuchProjectException::new);
        if (typeRoleProject == TypeRoleProject.ADMIN)
            return Optional.empty();
        if (isAdmin(project, user)) {
            return Optional.of(new AccessProjectResponse(accessProjectRepository.save(createAccessProject(
                    UUID.randomUUID().toString(), project, typeRoleProject, customProjectRoleId, disposable, liveTime))));
        } else throw new ForbiddenException();
    }

    @Transactional
    public void sendInvitationToMail(AccessProjectTroughMailRequest request, String adminLogin) throws NoSuchCustomRoleException, NoSuchProjectException, ForbiddenException {
        User admin = userRepository.findByUsername(adminLogin);
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow(NoSuchProjectException::new);
        if (isAdmin(project, admin)) {
            String token = UUID.randomUUID().toString();
            AccessProject accessProject = createAccessProject(token, project, request.getTypeRoleProject(),
                    request.getRoleId(), true, request.getLiveTimeInDays());
            userRepository.findByEmail(request.getEmail()).ifPresentOrElse(targetUser -> {
                mailService.sendInvitationToProject(targetUser.getEmail(), targetUser.getLocale(), project.getName(),
                        request.getUrl(), token);
                notificationService
                        .addNotificationAboutInvitationToProject(token, project.getName(), request.getUrl(), targetUser);
            }, () -> mailService.sendInvitationToProject(request.getEmail(), Locale.en, project.getName(),
                    request.getUrl(), token));
            accessProjectRepository.save(accessProject);
        } else {
            throw new ForbiddenException();
        }
    }

    private AccessProject createAccessProject(String token, Project project, TypeRoleProject typeRoleProject,
                                              long customProjectRoleId, boolean disposable, int liveTime) throws NoSuchCustomRoleException {
        AccessProject accessProject = new AccessProject();
        accessProject.setProject(project);
        accessProject.setTypeRoleProject(typeRoleProject);
        if (typeRoleProject == TypeRoleProject.CUSTOM_ROLE) {
            accessProject.setProjectRole(project.getAvailableRole().stream()
                    .filter(r -> r.getId() == customProjectRoleId)
                    .findAny()
                    .orElseThrow(NoSuchCustomRoleException::new));
        }
        accessProject.setDisposable(disposable);
        accessProject.setCode(token);
        accessProject.setTimeForDie(LocalDate.now().plusDays(liveTime).toEpochDay());
        return accessProject;
    }

    public Optional<ProjectResponse> findInfoOfProjectFromAccessToken(String token, int zoneId)
            throws AccessTokenInvalidException {
        AccessProject accessProject = accessProjectRepository.findById(token)
                .orElseThrow(AccessTokenInvalidException::new);
        if (LocalDate.ofEpochDay(accessProject.getTimeForDie()).isAfter(LocalDate.now())) {
            return Optional.of(new ProjectResponse(accessProject.getProject(),
                    (accessProject.getTypeRoleProject() == TypeRoleProject.CUSTOM_ROLE
                            ? accessProject.getProjectRole().getName()
                            : accessProject.getTypeRoleProject().name()), zoneId));
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    public boolean createAccessForUser(String token, String toUser) throws AccessTokenInvalidException {
        AccessProject accessProject = accessProjectRepository.findById(token)
                .orElseThrow(AccessTokenInvalidException::new);
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

    public void leave(long projectId, String userLogin) throws ForbiddenException, NoSuchProjectException {
        Project project = projectRepository.findById(projectId).orElseThrow(NoSuchProjectException::new);
        User user = userRepository.findByUsername(userLogin);
        visitMarkUpdater.deleteVisitMarkIfLeaveFromProject(projectId, user);
        if (project.getConnectors().size() == 1) {
            projectService.deleteProject(projectId, userLogin);
        } else {
            user.getUserWithProjectConnectors().parallelStream()
                    .filter(c -> c.getProject().equals(project))
                    .findAny().ifPresent(connector -> {
                        if (connector.getRoleType() != TypeRoleProject.ADMIN || project.getConnectors().stream()
                                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN).count() > 1) {
                            removeConnector(connector, project);
                        } else throw new IllegalActionException();
                    });
        }
    }

    public void kick(long projectId, long userId, String adminLogin) throws NoSuchProjectException, NoSuchUserException, ForbiddenException {
        User admin = userRepository.findByUsername(adminLogin);
        Project project = projectRepository.findById(projectId).orElseThrow(NoSuchProjectException::new);
        User user = userRepository.findById(userId).orElseThrow(NoSuchUserException::new);
        if (isAdmin(project, admin) && !isAdmin(project, user)) {
            visitMarkUpdater.deleteVisitMarkIfLeaveFromProject(projectId, user);
            notificationService.addNotificationAboutDeleteFromProject(project.getName(), user);
            user.getUserWithProjectConnectors().parallelStream()
                    .filter(c -> c.getProject().equals(project))
                    .forEach(c -> removeConnector(c, project));
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    void removeConnector(UserWithProjectConnector c, Project project) {
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

    private boolean isAdmin(Project project, User user) {
        return user.getUserWithProjectConnectors().stream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getProject().equals(project));
    }
}
