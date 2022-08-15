package ru.manager.ProgectManager.services.kanban;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanElementMainDataResponse;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.kanban.KanbanColumn;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.ElementStatus;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.kanban.IncorrectElementStatusException;
import ru.manager.ProgectManager.exception.kanban.NoSuchKanbanElementException;
import ru.manager.ProgectManager.exception.kanban.NoSuchKanbanException;
import ru.manager.ProgectManager.exception.runtime.IncorrectStatusException;
import ru.manager.ProgectManager.repositories.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class ArchiveAndTrashService {
    private final UserRepository userRepository;
    private final KanbanColumnRepository columnRepository;
    private final KanbanElementRepository elementRepository;
    private final TimeRemoverRepository timeRemoverRepository;
    private final KanbanRepository kanbanRepository;

    @Transactional
    public void finalDeleteElementFromTrash(long id) {
        KanbanElement element = elementRepository.findById(id).orElseThrow();
        if (element.getStatus() != ElementStatus.UTILISE)
            throw new IncorrectStatusException();

        if(element.getWorkTrackSet().isEmpty()) {
            KanbanColumn column = element.getKanbanColumn();
            column.getElements().remove(element);
            elementRepository.delete(element);
            columnRepository.save(column);
        } else {
            element.setStatus(ElementStatus.DELETED);
        }
    }

    @Transactional
    public void archive(long id, String userLogin)
            throws NoSuchKanbanElementException, IncorrectElementStatusException, ForbiddenException {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(id).orElseThrow(NoSuchKanbanElementException::new);
        if (element.getStatus() == ElementStatus.DELETED)
            throw new NoSuchKanbanElementException();
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditResource(kanban, user)) {
            if (element.getStatus() == ElementStatus.ARCHIVED)
                throw new IncorrectElementStatusException();

            timeRemoverRepository.findById(id).ifPresent(timeRemoverRepository::delete);

            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);
            element.setStatus(ElementStatus.ARCHIVED);
            KanbanColumn column = elementRepository.save(element).getKanbanColumn();
            column.getElements().stream()
                    .filter(e -> e.getSerialNumber() > element.getSerialNumber())
                    .forEach(e -> e.setSerialNumber(e.getSerialNumber() - 1));
            columnRepository.save(column);
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void reestablish(long id, String userLogin) throws ForbiddenException, NoSuchKanbanElementException, IncorrectElementStatusException {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(id).orElseThrow(NoSuchKanbanElementException::new);
        if (element.getStatus() == ElementStatus.DELETED)
            throw new NoSuchKanbanElementException();
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditResource(kanban, user)) {
            if (element.getStatus() == ElementStatus.ALIVE)
                throw new IncorrectElementStatusException();

            timeRemoverRepository.findById(id).ifPresent(timeRemoverRepository::delete);

            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);
            element.setStatus(ElementStatus.ALIVE);
            element.setSerialNumber(element.getKanbanColumn().getElements().stream()
                    .filter(e -> e.getStatus() == ElementStatus.ALIVE)
                    .mapToInt(KanbanElement::getSerialNumber)
                    .max().orElse(-1) + 1);
            elementRepository.save(element);
        } else {
            throw new ForbiddenException();
        }
    }

    public List<KanbanElementMainDataResponse> findArchive(long kanbanId, String userLogin)
            throws ForbiddenException, NoSuchKanbanException {
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = kanbanRepository.findById(kanbanId).orElseThrow(NoSuchKanbanException::new);
        if (canSeeResource(kanban, user)) {
            int zoneId = user.getZoneId();
            return kanban.getKanbanColumns().stream()
                    .flatMap(c -> c.getElements().stream())
                    .filter(e -> e.getStatus() == ElementStatus.ARCHIVED)
                    .map(e -> new KanbanElementMainDataResponse(e, zoneId))
                    .collect(Collectors.toList());
        } else {
            throw new ForbiddenException();
        }
    }

    public List<KanbanElementMainDataResponse> findTrash(long kanbanId, String userLogin) throws NoSuchKanbanException, ForbiddenException {
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = kanbanRepository.findById(kanbanId).orElseThrow(NoSuchKanbanException::new);
        if (canSeeResource(kanban, user)) {
            int zoneId = user.getZoneId();
            return kanban.getKanbanColumns().stream()
                    .flatMap(c -> c.getElements().stream())
                    .filter(e -> e.getStatus() == ElementStatus.UTILISE)
                    .map(e -> new KanbanElementMainDataResponse(e, zoneId))
                    .collect(Collectors.toList());
        } else {
            throw new ForbiddenException();
        }
    }

    private long getEpochSeconds() {
        return LocalDateTime.now().toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
    }

    private boolean canEditResource(Kanban kanban, User user){
        return kanban.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                .filter(CustomRoleWithKanbanConnector::isCanEdit)
                .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))));
    }

    private boolean canSeeResource(Kanban kanban, User user){
        return kanban.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))));
    }
}
