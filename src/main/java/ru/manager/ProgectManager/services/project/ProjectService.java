package ru.manager.ProgectManager.services.project;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.manager.ProgectManager.DTO.request.ProjectDataRequest;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.DTO.response.project.ProjectResponseWithFlag;
import ru.manager.ProgectManager.DTO.response.user.UserDataListResponse;
import ru.manager.ProgectManager.DTO.response.user.UserDataWithProjectRoleResponse;
import ru.manager.ProgectManager.components.PhotoCompressor;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.ResourceType;
import ru.manager.ProgectManager.enums.Size;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.project.NoSuchProjectException;
import ru.manager.ProgectManager.repositories.ProjectRepository;
import ru.manager.ProgectManager.repositories.UserRepository;
import ru.manager.ProgectManager.repositories.UserWithProjectConnectorRepository;
import ru.manager.ProgectManager.services.user.VisitMarkUpdater;

import java.io.IOException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log
@Transactional(readOnly = true)
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserWithProjectConnectorRepository connectorRepository;
    private final PhotoCompressor compressor;
    private final VisitMarkUpdater visitMarkUpdater;

    @Transactional
    public ProjectResponseWithFlag findProject(long id, String login) {
        User user = userRepository.findByUsername(login);
        Project project = projectRepository.findById(id).orElseThrow(NoSuchProjectException::new);
        if (project.getConnectors().stream().anyMatch(c -> c.getUser().equals(user))) {
            visitMarkUpdater.updateVisitMarks(user, project);
            return new ProjectResponseWithFlag(project, findUserRoleName(login, project.getId()),
                    canCreateOrDeleteResources(project.getId(), login));
        } else throw new ForbiddenException();
    }

    @Transactional
    public IdResponse addProject(ProjectDataRequest request, String userLogin) {
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
        return new IdResponse(project.getId());
    }

    @Transactional
    public void setPhoto(long id, MultipartFile photo, String userLogin) throws IOException {
        User admin = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(id).orElseThrow(NoSuchProjectException::new);
        if (isAdmin(project, admin)) {
            project.setPhoto(compressor.compress(photo, Size.MIDDLE));
            projectRepository.save(project);
        } else throw new ForbiddenException();
    }

    public byte[] findPhoto(long id) {
        return projectRepository.findById(id).orElseThrow().getPhoto();
    }

    @Transactional
    public void setData(long id, ProjectDataRequest request, String userLogin) {
        Project project = projectRepository.findById(id).orElseThrow(NoSuchProjectException::new);
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
        } else throw new ForbiddenException();
    }

    @Transactional
    public void deleteProject(long id, String adminLogin) {
        User admin = userRepository.findByUsername(adminLogin);
        Project project = projectRepository.findById(id).orElseThrow(NoSuchProjectException::new);
        if (isAdmin(project, admin)) {
            visitMarkUpdater.deleteVisitMark(project, project.getId(), ResourceType.PROJECT);
            project.getConnectors().forEach(connector -> {
                connector.setProject(null);
                connector.setUser(null);
                connectorRepository.delete(connector);
            });
            projectRepository.delete(project);
        } else throw new ForbiddenException();
    }

    public UserDataListResponse findAllMembers(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(id).orElseThrow(NoSuchProjectException::new);
        if (project.getConnectors().stream().map(UserWithProjectConnector::getUser).anyMatch(u -> u.equals(user))) {
            int zoneId = user.getZoneId();
            return new UserDataListResponse(project.getConnectors().stream()
                    .map(connector -> new UserDataWithProjectRoleResponse(connector.getUser(),
                            (connector.getRoleType() == TypeRoleProject.CUSTOM_ROLE
                                    ? connector.getCustomProjectRole().getName()
                                    : connector.getRoleType().name()),
                            zoneId))
                    .collect(Collectors.toList()));
        } else {
            throw new ForbiddenException();
        }
    }

    public UserDataListResponse findMembersByNicknameOrEmail(long id, String nicknameOrEmail, String userLogin) {
        String name = nicknameOrEmail.toLowerCase().trim();
        User user = userRepository.findByUsername(userLogin);
        Project project = projectRepository.findById(id).orElseThrow(NoSuchProjectException::new);
        if (project.getConnectors().stream().map(UserWithProjectConnector::getUser).anyMatch(u -> u.equals(user))) {
            int zoneId = user.getZoneId();
            return new UserDataListResponse(project.getConnectors().stream()
                    .filter(connector -> connector.getUser().getNickname().toLowerCase().contains(name)
                            || connector.getUser().getEmail().toLowerCase().contains(name))
                    .map(connector -> new UserDataWithProjectRoleResponse(connector.getUser(),
                            (connector.getRoleType() == TypeRoleProject.CUSTOM_ROLE
                                    ? connector.getCustomProjectRole().getName()
                                    : connector.getRoleType().name()),
                            zoneId))
                    .collect(Collectors.toList()));
        } else {
            throw new ForbiddenException();
        }
    }

    public boolean canCreateOrDeleteResources(long projectId, String userLogin) {
        return projectRepository.findById(projectId).orElseThrow(NoSuchProjectException::new).getConnectors()
                .parallelStream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN
                        || (c.getRoleType() == TypeRoleProject.CUSTOM_ROLE
                        && c.getCustomProjectRole().isCanEditResources()))
                .map(UserWithProjectConnector::getUser)
                .map(User::getUsername)
                .anyMatch(login -> login.equals(userLogin));
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

    private boolean isAdmin(Project project, User admin) {
        return project.getConnectors().stream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getUser().equals(admin));
    }
}
