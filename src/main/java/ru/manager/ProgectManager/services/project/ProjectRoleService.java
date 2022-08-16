package ru.manager.ProgectManager.services.project;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.manager.ProgectManager.DTO.request.accessProject.*;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.DTO.response.accessProject.CustomProjectRoleResponse;
import ru.manager.ProgectManager.DTO.response.user.PublicMainUserDataResponse;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.CustomProjectRole;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithDocumentConnector;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.documents.Page;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.documents.NoSuchPageException;
import ru.manager.ProgectManager.exception.kanban.NoSuchKanbanException;
import ru.manager.ProgectManager.exception.project.NoSuchCustomRoleException;
import ru.manager.ProgectManager.exception.project.NoSuchProjectException;
import ru.manager.ProgectManager.exception.user.NoSuchUserException;
import ru.manager.ProgectManager.repositories.*;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ProjectRoleService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final KanbanConnectorRepository kanbanConnectorRepository;
    private final CustomProjectRoleRepository customProjectRoleRepository;
    private final CustomRoleWithDocumentConnectorRepository documentConnectorRepository;
    private final AccessProjectRepository accessProjectRepository;
    private final UserWithProjectConnectorRepository projectConnectorRepository;

    @Transactional
    public Optional<IdResponse> createCustomRole(CreateCustomRoleRequest request, String userLogin)
            throws ForbiddenException, NoSuchProjectException, NoSuchKanbanException, NoSuchPageException {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow(NoSuchProjectException::new);
        if (isAdmin(project, user)) {
            if (containsRoleNameInProject(project, request.getName()))
                return Optional.empty();

            CustomProjectRole customProjectRole = new CustomProjectRole();
            customProjectRole.setProject(project);
            customProjectRole.setName(request.getName().trim());
            customProjectRole.setCanEditResources(request.isCanEditResource());
            CustomProjectRole savedRole = customProjectRoleRepository.save(customProjectRole);
            for (var kr : request.getKanbanConnectorRequests()) {
                CustomRoleWithKanbanConnector customRoleWithKanbanConnector = new CustomRoleWithKanbanConnector();
                customRoleWithKanbanConnector.setCanEdit(kr.isCanEdit());
                customRoleWithKanbanConnector.setKanban(project.getKanbans().parallelStream()
                        .filter(k -> k.getId() == kr.getId())
                        .findAny().orElseThrow(NoSuchKanbanException::new));
                customRoleWithKanbanConnector.setCustomProjectRole(savedRole);
                kanbanConnectorRepository.save(customRoleWithKanbanConnector);
            }
            for (var dr : request.getDocumentConnectorRequest()) {
                CustomRoleWithDocumentConnector customRoleWithDocumentConnector = new CustomRoleWithDocumentConnector();
                customRoleWithDocumentConnector.setCanEdit(dr.isCanEdit());
                customRoleWithDocumentConnector.setId(dr.getId());
                customRoleWithDocumentConnector.setPage(project.getPages().parallelStream()
                        .filter(p -> p.getRoot() == null)
                        .filter(p -> p.getId() == dr.getId())
                        .findAny().orElseThrow(NoSuchPageException::new));
                customRoleWithDocumentConnector.setCustomProjectRole(savedRole);
                documentConnectorRepository.save(customRoleWithDocumentConnector);
            }
            return Optional.of(new IdResponse(savedRole.getId()));
        } else throw new ForbiddenException();
    }

    public CustomProjectRoleResponse[] findAllCustomProjectRole(long projectId, String userLogin)
            throws ForbiddenException, NoSuchProjectException {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(projectId).orElseThrow(NoSuchProjectException::new);
        if (isAdmin(project, user)) {
            return project.getAvailableRole().parallelStream()
                    .map(CustomProjectRoleResponse::new)
                    .toArray(CustomProjectRoleResponse[]::new);
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void deleteCustomRole(long roleId, String userLogin) throws ForbiddenException, NoSuchProjectException {
        User user = userRepository.findByUsername(userLogin);
        CustomProjectRole role = customProjectRoleRepository.findById(roleId).orElseThrow(NoSuchProjectException::new);
        Project project = role.getProject();
        if (isAdmin(project, user)) {
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
                    .forEach(accessProjectRepository::delete); // удаление пригласительных ссылок, которые выдавали данную роль
            customProjectRoleRepository.delete(role);
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public boolean rename(long id, String name, String userLogin) throws ForbiddenException, NoSuchCustomRoleException {
        User user = userRepository.findByUsername(userLogin);
        CustomProjectRole customProjectRole = customProjectRoleRepository.findById(id)
                .orElseThrow(NoSuchCustomRoleException::new);
        Project project = customProjectRole.getProject();
        if (isAdmin(project, user)) {
            if (containsRoleNameInProject(project, name))
                return false;
            customProjectRole.setName(name);
            customProjectRoleRepository.save(customProjectRole);
            return true;
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void putCanEditResource(long id, boolean canEdit, String userLogin) throws ForbiddenException, NoSuchCustomRoleException {
        User user = userRepository.findByUsername(userLogin);
        CustomProjectRole customProjectRole = customProjectRoleRepository.findById(id)
                .orElseThrow(NoSuchCustomRoleException::new);
        if (isAdmin(customProjectRole.getProject(), user)) {
            customProjectRole.setCanEditResources(canEdit);
            customProjectRoleRepository.save(customProjectRole);
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void putKanbanConnections(PutConnectForResourceInRole request, String userLogin) throws NoSuchCustomRoleException, NoSuchKanbanException, ForbiddenException {
        CustomProjectRole customProjectRole = customProjectRoleRepository.findById(request.getRoleId())
                .orElseThrow(NoSuchCustomRoleException::new);
        User user = userRepository.findByUsername(userLogin);
        Project project = customProjectRole.getProject();
        if (isAdmin(project, user)) {
            // наличие данного соединения в роли
            Predicate<CustomRoleWithResourceConnectorRequest> containConnectorWithThisId = rc -> customProjectRole
                    .getCustomRoleWithKanbanConnectors()
                    .stream()
                    .map(CustomRoleWithKanbanConnector::getKanban)
                    .mapToLong(Kanban::getId)
                    .anyMatch(identity -> identity == rc.getId());
            request.getResourceConnector().stream()
                    .filter(containConnectorWithThisId)
                    .forEach(rc -> customProjectRole.getCustomRoleWithKanbanConnectors().stream()
                            .filter(c -> c.getKanban().getId() == rc.getId())
                            .forEach(c -> {
                                c.setCanEdit(rc.isCanEdit());
                                kanbanConnectorRepository.save(c);
                            })); // коннекторы, присутствующие в роли
            for (var rc : request.getResourceConnector().stream()
                    .filter(Predicate.not(containConnectorWithThisId)).collect(Collectors.toSet())) {
                CustomRoleWithKanbanConnector connector = new CustomRoleWithKanbanConnector();
                connector.setKanban(project.getKanbans().parallelStream()
                        .filter(k -> k.getId() == rc.getId())
                        .findAny()
                        .orElseThrow(NoSuchKanbanException::new));
                connector.setCanEdit(rc.isCanEdit());
                connector.setCustomProjectRole(customProjectRole);
                kanbanConnectorRepository.save(connector);
            }// коннекторы, ранее не присутствовавшие в роли
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void putPageConnections(PutConnectForResourceInRole request, String userLogin)
            throws ForbiddenException, NoSuchPageException, NoSuchCustomRoleException {
        CustomProjectRole customProjectRole = customProjectRoleRepository.findById(request.getRoleId())
                .orElseThrow(NoSuchCustomRoleException::new);
        User user = userRepository.findByUsername(userLogin);
        Project project = customProjectRole.getProject();
        if (isAdmin(project, user)) {
            // наличие данного соединения в роли
            Predicate<CustomRoleWithResourceConnectorRequest> containConnectorWithThisId = rc -> customProjectRole
                    .getCustomRoleWithDocumentConnectors()
                    .stream()
                    .map(CustomRoleWithDocumentConnector::getPage)
                    .mapToLong(Page::getId)
                    .anyMatch(identity -> identity == rc.getId());
            request.getResourceConnector().stream()
                    .filter(containConnectorWithThisId)
                    .forEach(rc -> customProjectRole.getCustomRoleWithDocumentConnectors().stream()
                            .filter(c -> c.getPage().getId() == rc.getId())
                            .forEach(c -> {
                                c.setCanEdit(rc.isCanEdit());
                                documentConnectorRepository.save(c);
                            })); // коннекторы, присутствующие в роли
            for (var rc : request.getResourceConnector().stream()
                    .filter(Predicate.not(containConnectorWithThisId)).collect(Collectors.toSet())) {
                CustomRoleWithDocumentConnector connector = new CustomRoleWithDocumentConnector();
                connector.setPage(project.getPages().parallelStream()
                        .filter(page -> page.getRoot() == null) // только корневые страницы
                        .filter(p -> p.getId() == rc.getId())
                        .findAny()
                        .orElseThrow(NoSuchPageException::new));
                connector.setCanEdit(rc.isCanEdit());
                connector.setCustomProjectRole(customProjectRole);
                documentConnectorRepository.save(connector);
            } // коннекторы, ранее не присутствовавшие в роли
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void deleteKanbanConnectors(DeleteConnectForResourceFromRole request, String userLogin)
            throws NoSuchCustomRoleException, ForbiddenException {
        CustomProjectRole customProjectRole = customProjectRoleRepository.findById(request.getRoleId())
                .orElseThrow(NoSuchCustomRoleException::new);
        User user = userRepository.findByUsername(userLogin);
        Project project = customProjectRole.getProject();
        if (isAdmin(project, user)) {
            request.getResourceId().forEach(id -> customProjectRole.getCustomRoleWithKanbanConnectors().stream()
                    .filter(connector -> connector.getKanban().getId() == id)
                    .findAny()
                    .ifPresent(kanbanConnectorRepository::delete));
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void deletePageConnectors(DeleteConnectForResourceFromRole request, String userLogin)
            throws NoSuchCustomRoleException, ForbiddenException {
        CustomProjectRole customProjectRole = customProjectRoleRepository.findById(request.getRoleId())
                .orElseThrow(NoSuchCustomRoleException::new);
        User user = userRepository.findByUsername(userLogin);
        Project project = customProjectRole.getProject();
        if (isAdmin(project, user)) {
            request.getResourceId().forEach(id -> customProjectRole.getCustomRoleWithDocumentConnectors().stream()
                    .filter(connector -> connector.getPage().getId() == id)
                    .findAny()
                    .ifPresent(documentConnectorRepository::delete));
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void editUserRole(EditUserRoleRequest request, String adminLogin)
            throws NoSuchProjectException, NoSuchUserException, ForbiddenException, NoSuchCustomRoleException {
        User admin = userRepository.findByUsername(adminLogin);
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow(NoSuchProjectException::new);
        if (isAdmin(project, admin)) {
            User targetUser = userRepository.findById(request.getUserId()).orElseThrow(NoSuchUserException::new);
            UserWithProjectConnector connector = targetUser.getUserWithProjectConnectors().stream()
                    .filter(c -> c.getProject().equals(project))
                    .findAny()
                    .orElseThrow(NoSuchUserException::new);
            connector.setRoleType(request.getTypeRoleProject());
            if (request.getTypeRoleProject() == TypeRoleProject.CUSTOM_ROLE) {
                connector.setCustomProjectRole(project.getAvailableRole().parallelStream()
                        .filter(role -> role.getId() == request.getRoleId())
                        .findAny().orElseThrow(NoSuchCustomRoleException::new));
            } else {
                connector.setCustomProjectRole(null);
            }
            projectConnectorRepository.save(connector);
        } else throw new ForbiddenException();
    }

    public PublicMainUserDataResponse[] findUsersOnRole(TypeRoleProject type, long roleId, long projectId, String userData,
                                               String userLogin) throws NoSuchProjectException, ForbiddenException { // user data - nickname or email
        String data = userData.trim().toLowerCase();
        User admin = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(projectId).orElseThrow(NoSuchProjectException::new);
        if (project.getConnectors().stream().map(UserWithProjectConnector::getUser).anyMatch(u -> u.equals(admin))) {
            int zoneId = admin.getZoneId();
            return switch (type) {
                case ADMIN -> project.getConnectors().parallelStream()
                        .filter(connector -> connector.getRoleType() == TypeRoleProject.ADMIN)
                        .map(UserWithProjectConnector::getUser)
                        .filter(user -> user.getNickname().toLowerCase().contains(data)
                                || user.getEmail().toLowerCase().contains(data))
                        .map(user -> new PublicMainUserDataResponse(user, zoneId))
                        .toArray(PublicMainUserDataResponse[]::new);
                case STANDARD_USER -> project.getConnectors().parallelStream()
                        .filter(connector -> connector.getRoleType() == TypeRoleProject.STANDARD_USER)
                        .map(UserWithProjectConnector::getUser)
                        .filter(user -> user.getNickname().toLowerCase().contains(data)
                                || user.getEmail().toLowerCase().contains(data))
                        .map(user -> new PublicMainUserDataResponse(user, zoneId))
                        .toArray(PublicMainUserDataResponse[]::new);
                case CUSTOM_ROLE -> project.getConnectors().parallelStream()
                        .filter(connector -> connector.getRoleType() == TypeRoleProject.CUSTOM_ROLE)
                        .filter(connector -> connector.getCustomProjectRole().getId() == roleId)
                        .map(UserWithProjectConnector::getUser)
                        .filter(user -> user.getNickname().toLowerCase().contains(data)
                                || user.getEmail().toLowerCase().contains(data))
                        .map(user -> new PublicMainUserDataResponse(user, zoneId))
                        .toArray(PublicMainUserDataResponse[]::new);
            };
        } else {
            throw new ForbiddenException();
        }
    }

    private boolean isAdmin(Project project, User user) {
        return user.getUserWithProjectConnectors().stream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getProject().equals(project));
    }

    private boolean containsRoleNameInProject(Project project, String inputName) {
        String newName = inputName.replace(" ", "").toLowerCase();
        if (newName.equals("administrator") || newName.equals("commonmember") || newName.equals("moderator")
                || newName.equals("модератор") || newName.equals("администратор")
                || newName.equals("обычныйпользователь")) {
            return true;
        }
        return project.getAvailableRole().parallelStream()
                .map(CustomProjectRole::getName)
                .anyMatch(name -> name.toLowerCase().equals(newName));
    }
}
