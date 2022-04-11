package ru.manager.ProgectManager.services.kanban;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.kanban.KanbanColumnRequest;
import ru.manager.ProgectManager.DTO.request.kanban.SortColumnRequest;
import ru.manager.ProgectManager.DTO.request.kanban.TransportColumnRequest;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.kanban.KanbanColumn;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.entitys.kanban.TimeRemover;
import ru.manager.ProgectManager.enums.SortType;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.repositories.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class KanbanColumnService {
    private final KanbanColumnRepository columnRepository;
    private final UserRepository userRepository;
    private final KanbanRepository kanbanRepository;
    private final KanbanElementRepository elementRepository;
    private final TimeRemoverRepository timeRemoverRepository;

    public boolean transportColumn(TransportColumnRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanColumn column = columnRepository.findById(request.getId()).get();
        Kanban kanban = column.getKanban();
        int from = column.getSerialNumber();
        if (canEditKanban(kanban, user)) {
            Set<KanbanColumn> allColumns = column.getKanban().getKanbanColumns();
            if (request.getTo() >= allColumns.size())
                throw new IllegalArgumentException();
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
        }
        return false;
    }

    public Optional<KanbanColumn> renameColumn(long id, String name, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanColumn kanbanColumn = columnRepository.findById(id).get();
        Kanban kanban = kanbanColumn.getKanban();
        if (canEditKanban(kanban, user)) {
            kanbanColumn.setName(name);
            return Optional.of(columnRepository.save(kanbanColumn));
        }
        return Optional.empty();
    }

    public boolean deleteColumn(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanColumn column = columnRepository.findById(id).get();
        Kanban kanban = column.getKanban();
        if (canEditKanban(kanban, user)) {
            kanban.getKanbanColumns().stream()
                    .filter(kanbanColumn -> kanbanColumn.getSerialNumber() > column.getSerialNumber())
                    .forEach(kanbanColumn -> kanbanColumn.setSerialNumber(kanbanColumn.getSerialNumber() - 1));
            kanban.getKanbanColumns().remove(column);
            columnRepository.delete(column);
            kanbanRepository.save(kanban);
            return true;
        }
        return false;
    }

    public Optional<KanbanColumn> addColumn(KanbanColumnRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = kanbanRepository.findById(request.getKanbanId()).get();
        Project project = kanban.getProject();
        if (canEditKanban(kanban, user)) {
            KanbanColumn kanbanColumn = new KanbanColumn();
            kanbanColumn.setName(request.getName());
            kanbanColumn.setKanban(kanban);
            kanbanColumn.setDelayedDays(0);
            kanban.getKanbanColumns().stream()
                    .max(Comparator.comparing(KanbanColumn::getSerialNumber))
                    .ifPresentOrElse(c -> kanbanColumn.setSerialNumber(c.getSerialNumber() + 1),
                            () -> kanbanColumn.setSerialNumber(0));

            kanban.getKanbanColumns().add(kanbanColumn);
            KanbanColumn result = columnRepository.save(kanbanColumn);
            kanbanRepository.save(kanban);
            return Optional.of(result);
        }
        return Optional.empty();
    }

    public Optional<KanbanColumn> sortColumn(SortColumnRequest sortColumnRequest, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanColumn column = columnRepository.findById(sortColumnRequest.getId()).get();
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
            return Optional.of(column);
        } else {
            return Optional.empty();
        }
    }

    public boolean setDelayDeleter(long id, int delay, String userLogin) {
        KanbanColumn column = columnRepository.findById(id).get();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = column.getKanban();
        if (canEditKanban(kanban, user)) {
            column.setDelayedDays(delay);
            column.getElements().stream().map(KanbanElement::getId).forEach(identity -> {
                TimeRemover timeRemover = timeRemoverRepository.findById(identity).get();
                timeRemover.setTimeToDelete(LocalDate.now().plusDays(delay).toEpochDay());
                timeRemoverRepository.save(timeRemover);
            });
            return true;
        } else {
            return false;
        }
    }

    public boolean removeDelayDeleter(long id, String userLogin) {
        KanbanColumn column = columnRepository.findById(id).get();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = column.getKanban();
        if (canEditKanban(kanban, user)) {
            column.setDelayedDays(0);
            column.getElements().stream().map(KanbanElement::getId).forEach(timeRemoverRepository::deleteById);
            return true;
        } else {
            return false;
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
