package ru.manager.ProgectManager.services.project;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.accessProject.AccessProjectTroughMailRequest;
import ru.manager.ProgectManager.DTO.request.accessProject.CreateCustomRoleRequest;
import ru.manager.ProgectManager.DTO.request.accessProject.EditUserRoleRequest;
import ru.manager.ProgectManager.DTO.response.ProjectResponse;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.*;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.IllegalActionException;
import ru.manager.ProgectManager.exception.NoSuchResourceException;
import ru.manager.ProgectManager.repositories.*;
import ru.manager.ProgectManager.services.MailService;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Service
@RequiredArgsConstructor
public class AccessProjectService {
    private final UserRepository userRepository;
    private final KanbanRepository kanbanRepository;
    private final AccessProjectRepository accessProjectRepository;
    private final ProjectRepository projectRepository;
    private final UserWithProjectConnectorRepository projectConnectorRepository;
    private final KanbanConnectorRepository kanbanConnectorRepository;
    private final CustomProjectRoleRepository customProjectRoleRepository;
    private final CustomRoleWithDocumentConnectorRepository documentConnectorRepository;
    private final MailService mailService;

    public Optional<CustomProjectRole> createCustomRole(CreateCustomRoleRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow();
        if (isAdmin(project, user)) {
            CustomProjectRole customProjectRole = new CustomProjectRole();
            setCustomProjectRoleData(project, customProjectRole, request);
            customProjectRole = customProjectRoleRepository.save(customProjectRole);
            project.getAvailableRole().add(customProjectRole);
            projectRepository.save(project);
            return Optional.of(customProjectRole);
        }
        return Optional.empty();
    }

    public Optional<Set<CustomProjectRole>> findAllCustomProjectRole(long projectId, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(projectId).orElseThrow();
        if (isAdmin(project, user)) {
            return Optional.of(project.getAvailableRole());
        } else {
            return Optional.empty();
        }
    }

    public boolean deleteCustomRole(long projectId, long roleId, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(projectId).orElseThrow();
        if (isAdmin(project, user)) {
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
            StreamSupport.stream(accessProjectRepository.findAll().spliterator(), true)
                    .filter(accessProject -> accessProject.getTypeRoleProject() == TypeRoleProject.CUSTOM_ROLE)
                    .filter(accessProject -> accessProject.getProjectRole().equals(role))
                    .forEach(accessProjectRepository::delete);
            project.getAvailableRole().remove(role);
            projectRepository.save(project);
            return true;
        } else {
            return false;
        }
    }

    public boolean changeRole(long roleId, CreateCustomRoleRequest newCustomRole, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(newCustomRole.getProjectId()).orElseThrow();
        if (isAdmin(project, user)) {
            CustomProjectRole customProjectRole = project.getAvailableRole().stream()
                    .filter(r -> r.getId() == roleId)
                    .findAny().orElseThrow(IllegalArgumentException::new);
            customProjectRole.getCustomRoleWithKanbanConnectors().clear();
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
        customProjectRole.setCustomRoleWithKanbanConnectors(request.getKanbanConnectorRequests().stream().map(kr -> {
            CustomRoleWithKanbanConnector customRoleWithKanbanConnector = new CustomRoleWithKanbanConnector();
            customRoleWithKanbanConnector.setCanEdit(kr.isCanEdit());
            customRoleWithKanbanConnector.setKanban(project.getKanbans().parallelStream()
                    .filter(k -> k.getId() == kr.getId())
                    .findAny().orElseThrow(() -> new NoSuchResourceException("Kanban: " + kr.getId())));
            return kanbanConnectorRepository.save(customRoleWithKanbanConnector);
        }).collect(Collectors.toSet()));
        customProjectRole.setCustomRoleWithDocumentConnectors(request.getDocumentConnectorRequest().stream().map(dr -> {
            CustomRoleWithDocumentConnector customRoleWithDocumentConnector = new CustomRoleWithDocumentConnector();
            customRoleWithDocumentConnector.setCanEdit(dr.isCanEdit());
            customRoleWithDocumentConnector.setId(dr.getId());
            customRoleWithDocumentConnector.setPage(project.getPages().parallelStream()
                    .filter(p -> p.getRoot() == null)
                    .filter(p -> p.getId() == dr.getId())
                    .findAny().orElseThrow(() -> new NoSuchResourceException("Page: " + dr.getId())));
            return documentConnectorRepository.save(customRoleWithDocumentConnector);
        }).collect(Collectors.toSet()));
    }

    public boolean editUserRole(EditUserRoleRequest request, String adminLogin) {
        User admin = userRepository.findByUsername(adminLogin);
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow();
        if (isAdmin(project, admin)) {
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
            projectConnectorRepository.save(connector);
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
        Project project = projectRepository.findById(projectId).orElseThrow();
        if (typeRoleProject == TypeRoleProject.ADMIN)
            throw new IllegalArgumentException();
        if (isAdmin(project, user)) {
            return Optional.of(accessProjectRepository.save(createAccessProject(UUID.randomUUID().toString(), project,
                    typeRoleProject, customProjectRoleId, disposable, liveTime)));
        }
        return Optional.empty();
    }

    public boolean sendInvitationToMail(AccessProjectTroughMailRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow();
        if(isAdmin(project, user)) {
            String token = UUID.randomUUID().toString();
            AccessProject accessProject = createAccessProject(token, project, request.getTypeRoleProject(),
                    request.getRoleId(), true, request.getLiveTimeInDays());
            mailService.sendInvitationToProject(user, project.getName(), request.getUrl(), token);
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
        if (accessProject.isDisposable() || LocalDate.ofEpochDay(accessProject.getTimeForDie()).isBefore(LocalDate.now())) {
            accessProjectRepository.delete(accessProject);
        }
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
                connector = projectConnectorRepository.save(connector);

                project.getConnectors().add(connector);
                user.getUserWithProjectConnectors().add(connector);
                projectRepository.save(project);
                userRepository.save(user);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean leave(long projectId, String userLogin) {
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isPresent()) {
            userRepository.findByUsername(userLogin).getUserWithProjectConnectors().parallelStream()
                    .filter(c -> c.getProject().equals(project.get()))
                    .findAny().ifPresent(connector -> {
                        if (connector.getRoleType() != TypeRoleProject.ADMIN || project.get().getConnectors().stream()
                                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN).count() > 1) {
                            removeConnector(connector, project.get());
                        } else throw new IllegalActionException();
                    });
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
