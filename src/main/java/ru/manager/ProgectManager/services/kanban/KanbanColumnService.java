package ru.manager.ProgectManager.services.kanban;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.KanbanColumnRequest;
import ru.manager.ProgectManager.DTO.request.TransportColumnRequest;
import ru.manager.ProgectManager.entitys.Kanban;
import ru.manager.ProgectManager.entitys.KanbanColumn;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.repositories.KanbanColumnRepository;
import ru.manager.ProgectManager.repositories.KanbanRepository;
import ru.manager.ProgectManager.repositories.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KanbanColumnService {
    private final KanbanColumnRepository columnRepository;
    private final UserRepository userRepository;
    private final KanbanRepository kanbanRepository;

    public boolean transportColumn(TransportColumnRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanColumn column = columnRepository.findById(request.getId()).get();
        int from = column.getSerialNumber();
        if (column.getKanban().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))) {
            List<KanbanColumn> allColumns = column.getKanban().getKanbanColumns();
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
            return true;
        }
        return false;
    }

    public Optional<KanbanColumn> renameColumn(long id, String name, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanColumn kanbanColumn = columnRepository.findById(id).get();
        if (kanbanColumn.getKanban().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))) {
            kanbanColumn.setName(name);
            return Optional.of(columnRepository.save(kanbanColumn));
        }
        return Optional.empty();
    }

    public boolean deleteColumn(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanColumn column = columnRepository.findById(id).get();
        Kanban kanban = column.getKanban();
        if (kanban.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))) {
            kanban.getKanbanColumns().stream()
                    .filter(kanbanColumn -> kanbanColumn.getSerialNumber() > column.getSerialNumber())
                    .forEach(kanbanColumn -> kanbanColumn.setSerialNumber(kanbanColumn.getSerialNumber() - 1));
            kanban.getKanbanColumns().remove(column);
            columnRepository.delete(column);
            return true;
        }
        return false;
    }

    public Optional<KanbanColumn> addColumn(KanbanColumnRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = kanbanRepository.findById(request.getKanbanId()).get();
        Project project = kanban.getProject();
        if (user.getUserWithProjectConnectors().stream().anyMatch(c -> c.getProject().equals(project))) {
            KanbanColumn kanbanColumn = new KanbanColumn();
            kanbanColumn.setName(request.getName());
            kanbanColumn.setKanban(kanban);
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

    public Optional<Kanban> findKanban(long id, String userLogin) {
        Kanban kanban = kanbanRepository.findById(id).get();
        User user = userRepository.findByUsername(userLogin);
        if (kanban.getProject().getConnectors().stream().anyMatch(p -> p.getUser().equals(user))) {
            return Optional.of(kanban);
        } else {
            return Optional.empty();
        }
    }

    public Kanban findKanbanFromColumn(long id) {
        return columnRepository.findById(id).get().getKanban();
    }
}
