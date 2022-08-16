package ru.manager.ProgectManager.services.kanban;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.manager.ProgectManager.DTO.request.kanban.TagRequest;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanContentResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanMainDataResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanMembers;
import ru.manager.ProgectManager.DTO.response.kanban.TagResponse;
import ru.manager.ProgectManager.DTO.response.user.PublicMainUserDataResponse;
import ru.manager.ProgectManager.components.PhotoCompressor;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.kanban.Tag;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.ResourceType;
import ru.manager.ProgectManager.enums.Size;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.kanban.NoSuchKanbanException;
import ru.manager.ProgectManager.exception.kanban.NoSuchTagException;
import ru.manager.ProgectManager.exception.project.NoSuchProjectException;
import ru.manager.ProgectManager.repositories.*;
import ru.manager.ProgectManager.services.project.AccessProjectService;
import ru.manager.ProgectManager.services.user.VisitMarkUpdater;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class KanbanService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final KanbanRepository kanbanRepository;
    private final CustomProjectRoleRepository customProjectRoleRepository;
    private final KanbanConnectorRepository kanbanConnectorRepository;
    private final TagRepository tagRepository;
    private final VisitMarkUpdater visitMarkUpdater;
    private final PhotoCompressor compressor;

    private final AccessProjectService accessProjectService;

    @Transactional
    public IdResponse createKanban(long projectId, String name, String userLogin)
            throws ForbiddenException, NoSuchProjectException {
        Project project = projectRepository.findById(projectId).orElseThrow(NoSuchProjectException::new);
        if (canEditResource(project, userRepository.findByUsername(userLogin))) {
            Kanban kanban = new Kanban();
            kanban.setProject(project);
            kanban.setName(name.trim());
            project.getKanbans().add(kanban);

            kanban = kanbanRepository.save(kanban);
            projectRepository.save(project);
            return new IdResponse(kanban.getId());
        } else throw new ForbiddenException();
    }

    @Transactional
    public void setImage(long kanbanId, MultipartFile image, String userLogin)
            throws IOException, NoSuchKanbanException, ForbiddenException {
        Kanban kanban = kanbanRepository.findById(kanbanId).orElseThrow(NoSuchKanbanException::new);
        User user = userRepository.findByUsername(userLogin);
        if (canEditKanban(kanban, user)) {
            kanban.setPhoto(compressor.compress(image, Size.LARGE));
            kanbanRepository.save(kanban);
        } else {
            throw new ForbiddenException();
        }
    }

    public byte[] findImage(long id) {
        return kanbanRepository.findById(id).orElseThrow().getPhoto();
    }

    @Transactional
    public void removeKanban(long id, String userLogin) throws NoSuchKanbanException, ForbiddenException {
        Kanban kanban = kanbanRepository.findById(id).orElseThrow(NoSuchKanbanException::new);
        Project project = kanban.getProject();
        User user = userRepository.findByUsername(userLogin);
        if (canEditResource(project, user) && canEditKanban(kanban, user)) {
            project.getAvailableRole().forEach(role -> role.getCustomRoleWithKanbanConnectors().stream()
                    .filter(kanbanConnector -> kanbanConnector.getKanban().getId() == id)
                    .forEach(kanbanConnector -> {
                        role.getCustomRoleWithKanbanConnectors().remove(kanbanConnector);
                        kanbanConnectorRepository.delete(kanbanConnector);
                        customProjectRoleRepository.save(role);
                    }));

            visitMarkUpdater.deleteVisitMark(project, kanban.getId(), ResourceType.KANBAN);

            project.getKanbans().remove(kanban);
            projectRepository.save(project);
        } else throw new ForbiddenException();
    }

    @Transactional
    public void rename(long id, String name, String userLogin) throws NoSuchKanbanException, ForbiddenException {
        Kanban kanban = kanbanRepository.findById(id).orElseThrow(NoSuchKanbanException::new);
        User user = userRepository.findByUsername(userLogin);
        if (canEditKanban(kanban, user)) {
            kanban.setName(name.trim());
            kanbanRepository.save(kanban);
            visitMarkUpdater.redactVisitMark(kanban);
        } else {
            throw new ForbiddenException();
        }
    }

    public KanbanContentResponse findKanban(long id, String userLogin, int pageIndex, int rowCount)
            throws NoSuchKanbanException, ForbiddenException {
        Kanban kanban = kanbanRepository.findById(id).orElseThrow(NoSuchKanbanException::new);
        User user = userRepository.findByUsername(userLogin);
        if (canSeeKanban(kanban, user)) {
            visitMarkUpdater.updateVisitMarks(user, kanban);
            return new KanbanContentResponse(kanban, pageIndex, rowCount,
                    accessProjectService.canEditKanban(id, userLogin));
        } else {
            throw new ForbiddenException();
        }
    }

    public KanbanMainDataResponse[] findAllKanban(long id, String userLogin)
            throws ForbiddenException, NoSuchProjectException {
        Project project = projectRepository.findById(id).orElseThrow(NoSuchProjectException::new);
        User user = userRepository.findByUsername(userLogin);
        Optional<UserWithProjectConnector> connector = user.getUserWithProjectConnectors().stream()
                .filter(c -> c.getProject().equals(project))
                .findAny();
        if (connector.isPresent()) {
            int zoneId = user.getZoneId();
            if (connector.get().getRoleType() == TypeRoleProject.CUSTOM_ROLE) {
                return connector.get().getCustomProjectRole().getCustomRoleWithKanbanConnectors().parallelStream()
                        .map(CustomRoleWithKanbanConnector::getKanban)
                        .map(kanban -> new KanbanMainDataResponse(kanban, zoneId))
                        .toArray(KanbanMainDataResponse[]::new);
            } else {
                return project.getKanbans().parallelStream()
                        .map(kanban -> new KanbanMainDataResponse(kanban, zoneId))
                        .toArray(KanbanMainDataResponse[]::new);
            }
        } else {
            throw new ForbiddenException();
        }
    }

    public KanbanMainDataResponse[] findKanbansByName(long id, String inputName, String userLogin)
            throws NoSuchProjectException, ForbiddenException {
        String name = inputName.trim().toLowerCase();
        Project project = projectRepository.findById(id).orElseThrow(NoSuchProjectException::new);
        User user = userRepository.findByUsername(userLogin);
        Optional<UserWithProjectConnector> connector = user.getUserWithProjectConnectors().stream()
                .filter(c -> c.getProject().equals(project))
                .findAny();
        if (connector.isPresent()) {
            int zoneId = user.getZoneId();
            if (connector.get().getRoleType() == TypeRoleProject.CUSTOM_ROLE) {
                return connector.get().getCustomProjectRole().getCustomRoleWithKanbanConnectors().parallelStream()
                        .map(CustomRoleWithKanbanConnector::getKanban)
                        .filter(k -> k.getName().toLowerCase().contains(name))
                        .map(kanban -> new KanbanMainDataResponse(kanban, zoneId))
                        .toArray(KanbanMainDataResponse[]::new);
            } else {
                return project.getKanbans().stream()
                        .filter(k -> k.getName().toLowerCase().contains(name))
                        .map(kanban -> new KanbanMainDataResponse(kanban, zoneId))
                        .toArray(KanbanMainDataResponse[]::new);
            }
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public IdResponse addTag(long id, TagRequest request, String userLogin)
            throws ForbiddenException, NoSuchKanbanException {
        Kanban kanban = kanbanRepository.findById(id).orElseThrow(NoSuchKanbanException::new);
        User user = userRepository.findByUsername(userLogin);
        if (canEditKanban(kanban, user)) {
            Tag tag = new Tag();
            tag.setColor(request.getColor());
            tag.setText(request.getText());
            tag.setKanban(kanban);
            tag = tagRepository.save(tag);
            kanban.getAvailableTags().add(tag);
            kanbanRepository.save(kanban);
            return new IdResponse(tag.getId());
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void removeTag(long id, String userLogin) throws NoSuchTagException, ForbiddenException {
        Tag tag = tagRepository.findById(id).orElseThrow(NoSuchTagException::new);
        Kanban kanban = tag.getKanban();
        User user = userRepository.findByUsername(userLogin);
        if (canEditKanban(kanban, user)) {
            kanban.getAvailableTags().remove(tag);
            kanban.getKanbanColumns().stream().flatMap(c -> c.getElements().stream())
                    .filter(e -> e.getTags().contains(tag))
                    .forEach(e -> e.getTags().remove(tag));
            tagRepository.delete(tag);
            kanbanRepository.save(kanban);
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void editTag(long id, TagRequest request, String userLogin) throws NoSuchTagException, ForbiddenException {
        Tag tag = tagRepository.findById(id).orElseThrow(NoSuchTagException::new);
        Kanban kanban = tag.getKanban();
        User user = userRepository.findByUsername(userLogin);
        if (canEditKanban(kanban, user)) {
            tag.setText(request.getText());
            tag.setColor(request.getColor());
            tagRepository.save(tag);
        } else {
            throw new ForbiddenException();
        }
    }

    public TagResponse[] findAllAvailableTags(long kanbanId, String userLogin)
            throws NoSuchKanbanException, ForbiddenException {
        Kanban kanban = kanbanRepository.findById(kanbanId).orElseThrow(NoSuchKanbanException::new);
        User user = userRepository.findByUsername(userLogin);
        if (canSeeKanban(kanban, user)) {
            return kanban.getAvailableTags().parallelStream().map(TagResponse::new).toArray(TagResponse[]::new);
        } else {
            throw new ForbiddenException();
        }
    }

    public KanbanMembers members(long id, String userLoin) throws NoSuchKanbanException, ForbiddenException {
        User user = userRepository.findByUsername(userLoin);
        Kanban kanban = kanbanRepository.findById(id).orElseThrow(NoSuchKanbanException::new);
        if (canSeeKanban(kanban, user)) {
            KanbanMembers kanbanMembers = new KanbanMembers();
            kanbanMembers.setBrowsingMembers(kanban.getProject().getConnectors().parallelStream()
                    .map(UserWithProjectConnector::getUser)
                    .filter(u -> canSeeKanban(kanban, u))
                    .filter(u -> !canEditKanban(kanban, u))
                    .map(u -> new PublicMainUserDataResponse(u, user.getZoneId()))
                    .collect(Collectors.toList()));
            kanbanMembers.setChangingMembers(kanban.getProject().getConnectors().parallelStream()
                    .map(UserWithProjectConnector::getUser)
                    .filter(u -> canEditKanban(kanban, u))
                    .map(u -> new PublicMainUserDataResponse(u, user.getZoneId()))
                    .collect(Collectors.toList()));
            return kanbanMembers;
        } else {
            throw new ForbiddenException();
        }
    }

    private boolean canEditResource(Project project, User user) {
        return user.getUserWithProjectConnectors()
                .stream().anyMatch(c -> c.getProject().equals(project)
                        && (c.getRoleType() == TypeRoleProject.ADMIN || (c.getRoleType() == TypeRoleProject.CUSTOM_ROLE
                        && c.getCustomProjectRole().isCanEditResources())));
    }

    private boolean canEditKanban(Kanban kanban, User user) {
        return kanban.getProject().getConnectors().parallelStream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().parallelStream()
                .filter(CustomRoleWithKanbanConnector::isCanEdit)
                .anyMatch(connector -> connector.getKanban().equals(kanban))));
    }

    private boolean canSeeKanban(Kanban kanban, User user) {
        return kanban.getProject().getConnectors().parallelStream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().parallelStream()
                .anyMatch(customRoleWithKanbanConnector -> customRoleWithKanbanConnector.getKanban().equals(kanban))));
    }
}
