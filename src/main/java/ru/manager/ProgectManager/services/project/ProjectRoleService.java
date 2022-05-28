package ru.manager.ProgectManager.services.project;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.accessProject.CreateCustomRoleRequest;
import ru.manager.ProgectManager.DTO.request.accessProject.EditUserRoleRequest;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.CustomProjectRole;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithDocumentConnector;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.NoSuchResourceException;
import ru.manager.ProgectManager.repositories.*;

import java.util.Optional;
import java.util.Set;
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
            CustomProjectRole customProjectRole = new CustomProjectRole();
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
                    .forEach(accessProjectRepository::delete);
            project.getAvailableRole().remove(role);
            projectRepository.save(project);
            return true;
        } else {
            return false;
        }
    }

    public boolean rename(long id, String name, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        CustomProjectRole customProjectRole = customProjectRoleRepository.findById(id).orElseThrow();
        if(isAdmin(customProjectRole.getProject(), user)) {
            customProjectRole.setName(name);
            customProjectRoleRepository.save(customProjectRole);
            return true;
        } else {
            return false;
        }
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

    private boolean isAdmin(Project project, User user) {
        return user.getUserWithProjectConnectors().stream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getProject().equals(project));
    }
}
