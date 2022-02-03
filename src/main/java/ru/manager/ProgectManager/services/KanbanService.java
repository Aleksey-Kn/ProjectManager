package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.*;
import ru.manager.ProgectManager.entitys.*;
import ru.manager.ProgectManager.repositories.KanbanColumnRepository;
import ru.manager.ProgectManager.repositories.KanbanElementRepository;
import ru.manager.ProgectManager.repositories.KanbanRepository;
import ru.manager.ProgectManager.repositories.UserRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KanbanService {
    private final KanbanColumnRepository columnRepository;
    private final KanbanElementRepository elementRepository;
    private final UserRepository userRepository;
    private final KanbanRepository kanbanRepository;

    public boolean addElement(CreateKanbanElementRequest request, String userLogin){
        KanbanColumn column = columnRepository.findById(request.getColumnId()).get();
        User user = userRepository.findByUsername(userLogin);
        if(column.getKanban().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
            KanbanElement element = new KanbanElement();
            element.setContent(request.getContent());
            element.setName(request.getName());
            element.setTag(request.getTag());

            element.setOwner(user);
            element.setLastRedactor(user);
            column.getElements().stream().max(Comparator.comparing(KanbanElement::getSerialNumber))
                    .ifPresentOrElse(e -> element.setSerialNumber(e.getSerialNumber() + 1),
                            () -> element.setSerialNumber(0));
            column.getElements().add(element);
            element.setKanbanColumn(column);
            elementRepository.save(element);
            columnRepository.save(column);
            return true;
        }
        return false;
    }

    public boolean setElement(long id, UpdateKanbanElementRequest request, String userLogin){
        KanbanElement element = elementRepository.findById(id).get();
        User user = userRepository.findByUsername(userLogin);
        if(element.getKanbanColumn().getKanban().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
            element.setContent(request.getContent());
            element.setName(request.getName());
            element.setTag(request.getTag());

            element.setLastRedactor(user);
            elementRepository.save(element);
            return true;
        }
        return false;
    }

    public boolean setPhoto(long id, String userLogin, byte[] photo){
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(id).get();
        if(element.getKanbanColumn().getKanban().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
            element.setPhoto(photo);
            elementRepository.save(element);
            return true;
        }
        return false;
    }

    public Optional<KanbanElement> getContentFromElement(long id, String userLogin){
        KanbanElement kanbanElement = elementRepository.findById(id).get();
        User user = userRepository.findByUsername(userLogin);
        if(kanbanElement
                .getKanbanColumn()
                .getKanban()
                .getProject()
                .getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
            return Optional.of(kanbanElement);
        }
        return Optional.empty();
    }

    public boolean transportColumn(TransportColumnRequest request, String userLogin){
        User user = userRepository.findByUsername(userLogin);
        KanbanColumn column = columnRepository.findById(request.getId()).get();
        int from = column.getSerialNumber();
        if (column.getKanban().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))) {
            List<KanbanColumn> allColumns = column.getKanban().getKanbanColumns();
            if(request.getTo() >= allColumns.size())
                throw new IllegalArgumentException("Index more collection size");
            if(request.getTo() > from) {
                allColumns.stream()
                        .filter(kanbanColumn -> kanbanColumn.getSerialNumber() > from)
                        .filter(kanbanColumn -> kanbanColumn.getSerialNumber() <= request.getTo())
                        .forEach(kanbanColumn -> kanbanColumn.setSerialNumber(kanbanColumn.getSerialNumber() - 1));
            } else{
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

    public boolean renameColumn(long id, String name, String userLogin){
        User user = userRepository.findByUsername(userLogin);
        KanbanColumn kanbanColumn = columnRepository.findById(id).get();
        if(kanbanColumn.getKanban().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
            kanbanColumn.setName(name);
            columnRepository.save(kanbanColumn);
            return true;
        }
        return false;
    }

    public boolean transportElement(TransportElementRequest request, String userLogin){
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(request.getId()).get();
        int from = element.getSerialNumber();
        if(element.getKanbanColumn().getKanban().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
            if(element.getKanbanColumn().getId() == request.getToColumn()) {
                List<KanbanElement> allElements = element.getKanbanColumn().getElements();
                if (request.getToIndex() >= allElements.size())
                    throw new IllegalArgumentException("Index more collection size");
                if (request.getToIndex() > from) {
                    allElements.stream()
                            .filter(kanbanElement -> kanbanElement.getSerialNumber() > from)
                            .filter(kanbanElement -> kanbanElement.getSerialNumber() <= request.getToIndex())
                            .forEach(kanbanElement -> kanbanElement.setSerialNumber(kanbanElement.getSerialNumber() - 1));
                } else {
                    allElements.stream()
                            .filter(kanbanElement -> kanbanElement.getSerialNumber() < from)
                            .filter(kanbanElement -> kanbanElement.getSerialNumber() >= request.getToIndex())
                            .forEach(kanbanElement -> kanbanElement.setSerialNumber(kanbanElement.getSerialNumber() + 1));
                }
            } else{
                KanbanColumn toColumn = columnRepository.findById((long) request.getToColumn()).get();
                List<KanbanElement> fromColumnElements = element.getKanbanColumn().getElements();
                List<KanbanElement> toColumnElements = toColumn.getElements();
                fromColumnElements.stream()
                        .filter(e -> e.getSerialNumber() > from)
                        .forEach(e -> e.setSerialNumber(e.getSerialNumber() - 1));
                toColumnElements.stream()
                        .filter(e -> e.getSerialNumber() >= request.getToIndex())
                        .forEach(e -> e.setSerialNumber(e.getSerialNumber() + 1));
                element.getKanbanColumn().getElements().remove(element);
                toColumn.getElements().add(element);
            }
            element.setSerialNumber(request.getToIndex());
            return true;
        }
        return false;
    }

    public boolean deleteColumn(long id, String userLogin){
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

    public boolean deleteElement(long id, String userLogin){
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(id).get();
        if(element.getKanbanColumn().getKanban().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
            element.getKanbanColumn().getElements().stream()
                    .filter(kanbanElement -> kanbanElement.getSerialNumber() > element.getSerialNumber())
                            .forEach(kanbanElement -> kanbanElement.setSerialNumber(kanbanElement.getSerialNumber() - 1));

            KanbanColumn column = element.getKanbanColumn();
            column.getElements().remove(element);
            columnRepository.save(column);

            elementRepository.delete(element);
            return true;
        }
        return false;
    }

    public boolean addColumn(KanbanColumnRequest request, String userLogin){
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = kanbanRepository.findById(request.getKanbanId()).get();
        if(user.getUserWithProjectConnectors().stream().anyMatch(c -> c.getProject().equals(kanban))) {
            KanbanColumn kanbanColumn = new KanbanColumn();
            kanbanColumn.setName(request.getName());
            kanbanColumn.setKanban(kanban);
            kanban.getKanbanColumns().stream()
                    .max(Comparator.comparing(KanbanColumn::getSerialNumber))
                    .ifPresentOrElse(c -> kanbanColumn.setSerialNumber(c.getSerialNumber() + 1),
                            () -> kanbanColumn.setSerialNumber(0));

            kanban.getKanbanColumns().add(kanbanColumn);
            columnRepository.save(kanbanColumn);
            kanbanRepository.save(kanban);
            return true;
        }
        return false;
    }

    public Optional<Kanban> findKanban(long id){
        return kanbanRepository.findById(id);
    }

    public Kanban findKanbanFromElement(long id){
        return elementRepository.findById(id).get().getKanbanColumn().getKanban();
    }

    public Kanban findKanbanFromColumn(long id){
        return columnRepository.findById(id).get().getKanban();
    }
}
