package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.*;
import ru.manager.ProgectManager.entitys.KanbanColumn;
import ru.manager.ProgectManager.entitys.KanbanElement;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.repositories.KanbanColumnRepository;
import ru.manager.ProgectManager.repositories.KanbanElementRepository;
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

    public boolean addElement(CreateKanbanElementRequest request, String userLogin){
        KanbanColumn column = columnRepository.findById(request.getColumnId()).get();
        User user = userRepository.findByUsername(userLogin);
        if(column.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
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
        if(element.getKanbanColumn().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
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
        if(element.getKanbanColumn().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
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
        if (column.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))) {
            List<KanbanColumn> allColumns = column.getProject().getKanbanColumns();
            if(from >= allColumns.size() || request.getTo() >= allColumns.size())
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
        if(kanbanColumn.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
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
        if(element.getKanbanColumn().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
            if(element.getKanbanColumn().getId() == request.getToColumn()) {
                List<KanbanElement> allElements = element.getKanbanColumn().getElements();
                if (from >= allElements.size() || request.getToIndex() >= allElements.size())
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
                element.setSerialNumber(request.getToIndex());
            } else{
                List<KanbanElement> fromColumnElements = element.getKanbanColumn().getElements();
                List<KanbanElement> toColumnElements = columnRepository.findById((long) request.getToColumn()).get()
                        .getElements();
                //TODO: реализовать перемещение эелмента между колонками
            }
            return true;
        }
        return false;
    }

    public boolean deleteColumn(long id, String userLogin){
        User user = userRepository.findByUsername(userLogin);
        KanbanColumn column = columnRepository.findById(id).get();
        Project project = column.getProject();
        if (project.getConnectors().stream().anyMatch(c -> c.getUser().equals(user))) {
            project.getKanbanColumns().stream()
                    .filter(kanbanColumn -> kanbanColumn.getSerialNumber() > column.getSerialNumber())
                            .forEach(kanbanColumn -> kanbanColumn.setSerialNumber(kanbanColumn.getSerialNumber() - 1));
            project.getKanbanColumns().remove(column);
            columnRepository.delete(column);
            return true;
        }
        return false;
    }

    public boolean deleteElement(long id, String userLogin){
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(id).get();
        if(element.getKanbanColumn().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
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

    public Project findProjectFromElement(long id){
        return elementRepository.findById(id).get().getKanbanColumn().getProject();
    }

    public Project findProjectFromColumn(long id){
        return columnRepository.findById(id).get().getProject();
    }
}
