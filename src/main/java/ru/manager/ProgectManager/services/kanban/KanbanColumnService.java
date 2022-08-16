package ru.manager.ProgectManager.services.kanban;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.manager.ProgectManager.DTO.request.kanban.KanbanColumnRequest;
import ru.manager.ProgectManager.DTO.request.kanban.SortColumnRequest;
import ru.manager.ProgectManager.DTO.request.kanban.TransportColumnRequest;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanColumnResponse;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.kanban.KanbanColumn;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.entitys.kanban.TimeRemover;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.SortType;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.kanban.NoSuchColumnException;
import ru.manager.ProgectManager.exception.kanban.NoSuchKanbanException;
import ru.manager.ProgectManager.repositories.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class KanbanColumnService {
    private final KanbanColumnRepository columnRepository;
    private final UserRepository userRepository;
    private final KanbanRepository kanbanRepository;
    private final KanbanElementRepository elementRepository;
    private final TimeRemoverRepository timeRemoverRepository;

    public KanbanColumnResponse findKanbanColumn(long id, String userLogin, int pageIndex, int rowCount)
            throws NoSuchColumnException, ForbiddenException {
        User user = userRepository.findByUsername(userLogin);
        KanbanColumn column = columnRepository.findById(id).orElseThrow(NoSuchColumnException::new);
        Kanban kanban = column.getKanban();
        if(kanban.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))))){
            return new KanbanColumnResponse(column, pageIndex, rowCount, user.getZoneId());
        } else{
            throw new ForbiddenException();
        }
    }

    @Transactional
    public boolean transportColumn(TransportColumnRequest request, String userLogin) throws NoSuchColumnException, ForbiddenException {
        User user = userRepository.findByUsername(userLogin);
        KanbanColumn column = columnRepository.findById(request.getId()).orElseThrow(NoSuchColumnException::new);
        Kanban kanban = column.getKanban();
        int from = column.getSerialNumber();
        if (canEditKanban(kanban, user)) {
            Set<KanbanColumn> allColumns = column.getKanban().getKanbanColumns();
            if (request.getTo() >= allColumns.size())
                return false;
            if (request.getTo() > from) {
                allColumns.stream()
                        .filter(kanbanColumn -> kanbanColumn.getSerialNumber() > from)
                        .filter(kanbanColumn -> kanbanColumn.getSerialNumber() <= request.getTo())
                        .forEach(kanbanColumn -> kanbanColumn.setSerialNumber(kanbanColumn.getSerialNumber() - 1));
            } else {
                allColumns.stream()
                        .filter(kanbanColumn -> kanbanColumn.getSerialNumber() < from)
                        .filter(kanbanColumn -> kanbanColumn.getSerialNumber() >= request.getTo())
                        .forEach(kanbanColumn -> kanbanColumn.setSerialNumber(kanbanColumn.getSerialNumber() + 1));
            }
            column.setSerialNumber(request.getTo());

            columnRepository.saveAll(allColumns);
            return true;
        } else throw new ForbiddenException();
    }

    @Transactional
    public void renameColumn(long id, String name, String userLogin) throws ForbiddenException, NoSuchColumnException {
        User user = userRepository.findByUsername(userLogin);
        KanbanColumn kanbanColumn = columnRepository.findById(id).orElseThrow(NoSuchColumnException::new);
        Kanban kanban = kanbanColumn.getKanban();
        if (canEditKanban(kanban, user)) {
            kanbanColumn.setName(name.trim());
            columnRepository.save(kanbanColumn);
        } else throw new ForbiddenException();
    }

    @Transactional
    public void deleteColumn(long id, String userLogin) throws NoSuchColumnException, ForbiddenException {
        User user = userRepository.findByUsername(userLogin);
        KanbanColumn column = columnRepository.findById(id).orElseThrow(NoSuchColumnException::new);
        Kanban kanban = column.getKanban();
        if (canEditKanban(kanban, user)) {
            kanban.getKanbanColumns().stream()
                    .filter(kanbanColumn -> kanbanColumn.getSerialNumber() > column.getSerialNumber())
                    .forEach(kanbanColumn -> kanbanColumn.setSerialNumber(kanbanColumn.getSerialNumber() - 1));
            kanban.getKanbanColumns().remove(column);
            columnRepository.delete(column);
            kanbanRepository.save(kanban);
        } else throw new ForbiddenException();
    }

    @Transactional
    public IdResponse addColumn(KanbanColumnRequest request, String userLogin)
            throws ForbiddenException, NoSuchKanbanException {
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = kanbanRepository.findById(request.getKanbanId()).orElseThrow(NoSuchKanbanException::new);
        if (canEditKanban(kanban, user)) {
            KanbanColumn kanbanColumn = new KanbanColumn();
            kanbanColumn.setName(request.getName().trim());
            kanbanColumn.setKanban(kanban);
            kanbanColumn.setDelayedDays(0);
            kanban.getKanbanColumns().stream()
                    .max(Comparator.comparing(KanbanColumn::getSerialNumber))
                    .ifPresentOrElse(c -> kanbanColumn.setSerialNumber(c.getSerialNumber() + 1),
                            () -> kanbanColumn.setSerialNumber(0));

            kanban.getKanbanColumns().add(kanbanColumn);
            KanbanColumn result = columnRepository.save(kanbanColumn);
            kanbanRepository.save(kanban);
            return new IdResponse(result.getId());
        } else throw new ForbiddenException();
    }

    @Transactional
    public KanbanColumnResponse sortColumn(SortColumnRequest sortColumnRequest, String userLogin,
                                           int pageIndex, int rowCount) throws ForbiddenException, NoSuchColumnException {
        User user = userRepository.findByUsername(userLogin);
        KanbanColumn column = columnRepository.findById(sortColumnRequest.getId())
                .orElseThrow(NoSuchColumnException::new);
        Kanban kanban = column.getKanban();
        if (canEditKanban(kanban, user)) {
            Comparator<KanbanElement> comparator;
            if (sortColumnRequest.getType() == SortType.ALPHABET) {
                comparator = Comparator.comparing(KanbanElement::getName);
            } else {
                comparator = Comparator.comparing(sortColumnRequest.getType() == SortType.TIME_CREATE ?
                        KanbanElement::getTimeOfCreate : KanbanElement::getTimeOfUpdate);
            }
            if (sortColumnRequest.isReverse()) {
                comparator = comparator.reversed();
            }
            KanbanElement[] elements = column.getElements().toArray(KanbanElement[]::new);
            Arrays.sort(elements, comparator);
            for (int i = 0; i < elements.length; i++) {
                elements[i].setSerialNumber(i);
            }
            elementRepository.saveAll(Set.of(elements));
            return new KanbanColumnResponse(column, pageIndex, rowCount, user.getZoneId());
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void setDelayDeleter(long id, int delay, String userLogin) throws ForbiddenException, NoSuchColumnException {
        KanbanColumn column = columnRepository.findById(id).orElseThrow(NoSuchColumnException::new);
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = column.getKanban();
        if (canEditKanban(kanban, user)) {
            column.setDelayedDays(delay);
            column.getElements().stream().map(KanbanElement::getId).forEach(identity -> {
                TimeRemover timeRemover = timeRemoverRepository.findById(identity).orElseThrow();
                timeRemover.setTimeToDelete(LocalDate.now().plusDays(delay).toEpochDay());
                timeRemoverRepository.save(timeRemover);
            });
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void removeDelayDeleter(long id, String userLogin) throws NoSuchColumnException, ForbiddenException {
        KanbanColumn column = columnRepository.findById(id).orElseThrow(NoSuchColumnException::new);
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = column.getKanban();
        if (canEditKanban(kanban, user)) {
            column.setDelayedDays(0);
            column.getElements().stream().map(KanbanElement::getId).forEach(timeRemoverRepository::deleteById);
        } else {
            throw new ForbiddenException();
        }
    }

    private boolean canEditKanban(Kanban kanban, User user){
        return kanban.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                .filter(CustomRoleWithKanbanConnector::isCanEdit)
                .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))));
    }
}
