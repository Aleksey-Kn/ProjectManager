package ru.manager.ProgectManager.services.project;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.manager.ProgectManager.DTO.request.accessProject.*;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.CustomProjectRole;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithDocumentConnector;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.documents.Page;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.NoSuchResourceException;
import ru.manager.ProgectManager.repositories.*;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    public Optional<CustomProjectRole> createCustomRole(CreateCustomRoleRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow();
        if (isAdmin(project, user)) {
            if(containsRoleNameInProject(project, request.getName()))
                throw new IllegalArgumentException();

            CustomProjectRole customProjectRole = new CustomProjectRole();
            customProjectRole.setProject(project);
            customProjectRole.setName(request.getName().trim());
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
            return Optional.of(customProjectRoleRepository.save(customProjectRole));
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

    public boolean deleteCustomRole(long roleId, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        CustomProjectRole role = customProjectRoleRepository.findById(roleId).orElseThrow();
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
            return true;
        } else {
            return false;
        }
    }

    public boolean rename(long id, String name, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        CustomProjectRole customProjectRole = customProjectRoleRepository.findById(id).orElseThrow();
        Project project = customProjectRole.getProject();
        if (isAdmin(project, user)) {
            if(containsRoleNameInProject(project, name))
                throw new IllegalArgumentException();
            customProjectRole.setName(name);
            customProjectRoleRepository.save(customProjectRole);
            return true;
        } else {
            return false;
        }
    }

    public boolean putCanEditResource(long id, boolean canEdit, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        CustomProjectRole customProjectRole = customProjectRoleRepository.findById(id).orElseThrow();
        if (isAdmin(customProjectRole.getProject(), user)) {
            customProjectRole.setCanEditResources(canEdit);
            customProjectRoleRepository.save(customProjectRole);
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public boolean putKanbanConnections(PutConnectForResourceInRole request, String userLogin) {
        CustomProjectRole customProjectRole = customProjectRoleRepository.findById(request.getRoleId()).orElseThrow();
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
            request.getResourceConnector().stream()
                    .filter(Predicate.not(containConnectorWithThisId))
                    .forEach(rc -> {
                        CustomRoleWithKanbanConnector connector = new CustomRoleWithKanbanConnector();
                        connector.setKanban(project.getKanbans().parallelStream()
                                .filter(k -> k.getId() == rc.getId())
                                .findAny()
                                .orElseThrow(NoSuchResourceException::new));
                        connector.setCanEdit(rc.isCanEdit());
                        customProjectRole.getCustomRoleWithKanbanConnectors()
                                .add(kanbanConnectorRepository.save(connector));
                    });// коннекторы, ранее не присутствовавшие в роли
            customProjectRoleRepository.save(customProjectRole);
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public boolean putPageConnections(PutConnectForResourceInRole request, String userLogin) {
        CustomProjectRole customProjectRole = customProjectRoleRepository.findById(request.getRoleId()).orElseThrow();
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
            request.getResourceConnector().stream()
                    .filter(Predicate.not(containConnectorWithThisId))
                    .forEach(rc -> {
                        CustomRoleWithDocumentConnector connector = new CustomRoleWithDocumentConnector();
                        connector.setPage(project.getPages().parallelStream()
                                .filter(page -> page.getRoot() == null) // только корневые страницы
                                .filter(p -> p.getId() == rc.getId())
                                .findAny()
                                .orElseThrow(NoSuchResourceException::new));
                        connector.setCanEdit(rc.isCanEdit());
                        customProjectRole.getCustomRoleWithDocumentConnectors()
                                .add(documentConnectorRepository.save(connector));
                    });// коннекторы, ранее не присутствовавшие в роли
            customProjectRoleRepository.save(customProjectRole);
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteKanbanConnectors(DeleteConnectForResourceFromRole request, String userLogin) {
        CustomProjectRole customProjectRole = customProjectRoleRepository.findById(request.getRoleId()).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Project project = customProjectRole.getProject();
        if (isAdmin(project, user)) {
            request.getResourceId().forEach(id -> customProjectRole.getCustomRoleWithKanbanConnectors().stream()
                    .filter(connector -> connector.getKanban().getId() == id)
                    .findAny()
                    .ifPresent(kanbanConnector -> {
                        customProjectRole.getCustomRoleWithKanbanConnectors().remove(kanbanConnector);
                        kanbanConnectorRepository.delete(kanbanConnector);
                    }));
            return true;
        } else {
            return false;
        }
    }

    public boolean deletePageConnectors(DeleteConnectForResourceFromRole request, String userLogin) {
        CustomProjectRole customProjectRole = customProjectRoleRepository.findById(request.getRoleId()).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Project project = customProjectRole.getProject();
        if (isAdmin(project, user)) {
            request.getResourceId().forEach(id -> customProjectRole.getCustomRoleWithDocumentConnectors().stream()
                    .filter(connector -> connector.getPage().getId() == id)
                    .findAny()
                    .ifPresent(pageConnector -> {
                        customProjectRole.getCustomRoleWithDocumentConnectors().remove(pageConnector);
                        documentConnectorRepository.delete(pageConnector);
                    }));
            return true;
        } else {
            return false;
        }
    }

    public boolean editUserRole(EditUserRoleRequest request, String adminLogin) {
        User admin = userRepository.findByUsername(adminLogin);
        CustomProjectRole role = customProjectRoleRepository.findById(request.getRoleId()).orElseThrow();
        Project project = role.getProject();
        if (isAdmin(project, admin)) {
            User targetUser = userRepository.findById(request.getUserId()).orElseThrow(NoSuchResourceException::new);
            UserWithProjectConnector connector = targetUser.getUserWithProjectConnectors().stream()
                    .filter(c -> c.getProject().equals(project))
                    .findAny()
                    .orElseThrow(() -> new NoSuchResourceException("Project connect with user: " + request.getUserId()));
            connector.setRoleType(request.getTypeRoleProject());
            if (request.getTypeRoleProject() == TypeRoleProject.CUSTOM_ROLE) {
                connector.setCustomProjectRole(role);
            } else {
                connector.setCustomProjectRole(null);
            }
            projectConnectorRepository.save(connector);
            return true;
        }
        return false;
    }

    private boolean isAdmin(Project project, User user) {
        return user.getUserWithProjectConnectors().stream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getProject().equals(project));
    }

    private boolean containsRoleNameInProject(Project project, String newName) {
        return project.getAvailableRole().parallelStream()
                .map(CustomProjectRole::getName)
                .anyMatch(name -> name.equals(newName));
    }
}
