package ru.manager.ProgectManager.services.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.manager.ProgectManager.DTO.request.ProjectDataRequest;
import ru.manager.ProgectManager.DTO.response.user.UserDataListResponse;
import ru.manager.ProgectManager.DTO.response.user.UserDataWithProjectRoleResponse;
import ru.manager.ProgectManager.components.PhotoCompressor;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.ResourceType;
import ru.manager.ProgectManager.enums.Size;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.repositories.ProjectRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.UserWithProjectConnectorRepository;
import ru.manager.ProgectManager.services.user.VisitMarkUpdater;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserWithProjectConnectorRepository connectorRepository;
    private final PhotoCompressor compressor;
    private final VisitMarkUpdater visitMarkUpdater;

    public Optional<Project> findProject(long id, String login) {
        User user = userRepository.findByUsername(login);
        Project project = projectRepository.findById(id).orElseThrow();
        if (project.getConnectors().stream().anyMatch(c -> c.getUser().equals(user))) {
            visitMarkUpdater.updateVisitMarks(user, project);
            return Optional.of(project);
        }
        return Optional.empty();
    }

    public Project addProject(ProjectDataRequest request, String userLogin) {
        User owner = userRepository.findByUsername(userLogin);

        Project project = new Project();
        project.setName(request.getName().trim());
        project.setStatus(request.getStatus());
        project.setStatusColor(request.getStatusColor());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setDeadline(request.getDeadline());
        project = projectRepository.save(project);

        UserWithProjectConnector connector = new UserWithProjectConnector();
        connector.setRoleType(TypeRoleProject.ADMIN);
        connector.setProject(project);
        connector.setUser(owner);
        connectorRepository.save(connector);
        return project;
    }

    public boolean setPhoto(long id, MultipartFile photo, String userLogin) throws IOException {
        User admin = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(id).orElseThrow();
        if (isAdmin(project, admin)) {
            project.setPhoto(compressor.compress(photo, Size.MIDDLE));
            projectRepository.save(project);
            return true;
        }
        return false;
    }

    public byte[] findPhoto(long id) {
        return projectRepository.findById(id).orElseThrow().getPhoto();
    }

    public boolean setData(long id, ProjectDataRequest request, String userLogin) {
        Project project = projectRepository.findById(id).orElseThrow();
        User admin = userRepository.findByUsername(userLogin);
        if (isAdmin(project, admin)) {
            project.setName(request.getName().trim());
            project.setStatus(request.getStatus());
            project.setStatusColor(request.getStatusColor());
            project.setDescription(request.getDescription());
            project.setStartDate(request.getStartDate());
            project.setDeadline(request.getDeadline());
            projectRepository.save(project);
            visitMarkUpdater.redactVisitMark(project);
            return true;
        }
        return false;
    }

    public boolean deleteProject(long id, String adminLogin) {
        User admin = userRepository.findByUsername(adminLogin);
        Project project = projectRepository.findById(id).orElseThrow();
        if (isAdmin(project, admin)) {
            visitMarkUpdater.deleteVisitMark(project, project.getId(), ResourceType.PROJECT);
            project.getConnectors().forEach(connector -> {
                connector.setProject(null);
                connector.setUser(null);
                connectorRepository.delete(connector);
            });
            projectRepository.delete(project);
            return true;
        }
        return false;
    }

    public Optional<UserDataListResponse> findAllMembers(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(id).orElseThrow();
        if (project.getConnectors().stream().map(UserWithProjectConnector::getUser).anyMatch(u -> u.equals(user))) {
            int zoneId = user.getZoneId();
            return Optional.of(new UserDataListResponse(project.getConnectors().stream()
                    .map(connector -> new UserDataWithProjectRoleResponse(connector.getUser(),
                            (connector.getRoleType() == TypeRoleProject.CUSTOM_ROLE
                                    ? connector.getCustomProjectRole().getName()
                                    : connector.getRoleType().name()),
                            zoneId))
                    .collect(Collectors.toList())));
        } else {
            return Optional.empty();
        }
    }

    public Optional<UserDataListResponse> findMembersByNicknameOrEmail(long id, String nicknameOrEmail, String userLogin) {
        String name = nicknameOrEmail.toLowerCase().trim();
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(id).orElseThrow();
        if (project.getConnectors().stream().map(UserWithProjectConnector::getUser).anyMatch(u -> u.equals(user))) {
            int zoneId = user.getZoneId();
            return Optional.of(new UserDataListResponse(project.getConnectors().stream()
                    .filter(connector -> connector.getUser().getNickname().toLowerCase().contains(name)
                            || connector.getUser().getEmail().toLowerCase().contains(name))
                    .map(connector -> new UserDataWithProjectRoleResponse(connector.getUser(),
                            (connector.getRoleType() == TypeRoleProject.CUSTOM_ROLE
                                    ? connector.getCustomProjectRole().getName()
                                    : connector.getRoleType().name()),
                            zoneId))
                    .collect(Collectors.toList())));
        } else {
            return Optional.empty();
        }
    }

    public boolean canCreateOrDeleteResources(Project project, String userLogin) {
        return project.getConnectors().parallelStream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN
                        || (c.getRoleType() == TypeRoleProject.CUSTOM_ROLE
                        && c.getCustomProjectRole().isCanEditResources()))
                .map(UserWithProjectConnector::getUser)
                .map(User::getUsername)
                .anyMatch(login -> login.equals(userLogin));
    }

    private boolean isAdmin(Project project, User admin) {
        return project.getConnectors().stream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getUser().equals(admin));
    }
}
