package ru.manager.ProgectManager.services.kanban;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.kanban.CreateKanbanElementRequest;
import ru.manager.ProgectManager.DTO.request.kanban.TransportElementRequest;
import ru.manager.ProgectManager.DTO.request.kanban.UpdateKanbanElementRequest;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.kanban.Kanban;
import ru.manager.ProgectManager.entitys.kanban.KanbanColumn;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.entitys.kanban.TimeRemover;
import ru.manager.ProgectManager.enums.ElementStatus;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.IncorrectStatusException;
import ru.manager.ProgectManager.exception.NoSuchResourceException;
import ru.manager.ProgectManager.repositories.KanbanColumnRepository;
import ru.manager.ProgectManager.repositories.KanbanElementRepository;
import ru.manager.ProgectManager.repositories.TimeRemoverRepository;
import ru.manager.ProgectManager.repositories.UserRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class KanbanElementService {
    private final KanbanColumnRepository columnRepository;
    private final KanbanElementRepository elementRepository;
    private final UserRepository userRepository;
    private final TimeRemoverRepository timeRemoverRepository;

    public Optional<KanbanElement> addElement(CreateKanbanElementRequest request, String userLogin) {
        KanbanColumn column = columnRepository.findById(request.getColumnId()).get();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = column.getKanban();
        if (canEditKanban(kanban, user)) {
            KanbanElement element = new KanbanElement();
            element.setContent(request.getContent());
            element.setName(request.getName());
            element.setOwner(user);
            element.setLastRedactor(user);
            element.setKanbanColumn(column);
            element.setStatus(ElementStatus.ALIVE);
            element.setTimeOfCreate(getEpochSeconds());
            element.setTimeOfUpdate(getEpochSeconds());
            element.setSelectedDate(request.getDate());

            createSoftRemover(column, element);

            column.getElements().stream().max(Comparator.comparing(KanbanElement::getSerialNumber))
                    .ifPresentOrElse(e -> element.setSerialNumber(e.getSerialNumber() + 1),
                            () -> element.setSerialNumber(0));
            column.getElements().add(element);
            KanbanElement kanbanElement = elementRepository.save(element);
            columnRepository.save(column);
            return Optional.of(kanbanElement);
        }
        return Optional.empty();
    }

    public Optional<KanbanElement> setElement(long id, UpdateKanbanElementRequest request, String userLogin) {
        KanbanElement element = elementRepository.findById(id).get();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            if (element.getStatus() == ElementStatus.UTILISE)
                throw new IncorrectStatusException();
            element.setContent(request.getContent());
            element.setName(request.getName());
            element.setTimeOfUpdate(getEpochSeconds());
            element.setSelectedDate(request.getDate());

            element.setLastRedactor(user);
            return Optional.of(elementRepository.save(element));
        }
        return Optional.empty();
    }

    public boolean transportElement(TransportElementRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(request.getId()).get();
        int from = element.getSerialNumber();
        KanbanColumn fromColumn = element.getKanbanColumn();
        Kanban kanban = fromColumn.getKanban();
        if (canEditKanban(kanban, user)) {
            if (element.getStatus() == ElementStatus.ALIVE) {
                if (fromColumn.getId() == request.getToColumn()) {
                    Set<KanbanElement> allElements = fromColumn.getElements();
                    if (request.getToIndex() >= allElements.size())
                        throw new IllegalArgumentException();
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
                    element.setTimeOfUpdate(getEpochSeconds());

                    elementRepository.saveAll(allElements);
                } else {
                    KanbanColumn toColumn = columnRepository.findById((long) request.getToColumn()).get();
                    Set<KanbanElement> fromColumnElements = fromColumn.getElements();
                    Set<KanbanElement> toColumnElements = toColumn.getElements();
                    fromColumnElements.stream()
                            .filter(e -> e.getSerialNumber() > from)
                            .forEach(e -> e.setSerialNumber(e.getSerialNumber() - 1));
                    toColumnElements.stream()
                            .filter(e -> e.getSerialNumber() >= request.getToIndex())
                            .forEach(e -> e.setSerialNumber(e.getSerialNumber() + 1));
                    fromColumn.getElements().remove(element);
                    toColumn.getElements().add(element);

                    element.setSerialNumber(request.getToIndex());
                    element.setTimeOfUpdate(getEpochSeconds());
                    element.setKanbanColumn(toColumn);

                    if (fromColumn.getDelayedDays() != 0) {
                        timeRemoverRepository.deleteById(element.getId());
                    }
                    createSoftRemover(toColumn, element);

                    columnRepository.save(fromColumn);
                    columnRepository.save(toColumn);
                }
                return true;
            } else throw new IncorrectStatusException();
        }
        return false;
    }

    public Optional<KanbanColumn> utilizeElementFromUser(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(id).get();
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            TimeRemover timeRemover;
            if (element.getKanbanColumn().getDelayedDays() == 0) {
                timeRemover = new TimeRemover();
                timeRemover.setRemoverId(element.getId());
            } else { // если пользователь улаляет элемент из колонки с регулярным удалением вручную, то удалитель уже есть
                timeRemover = timeRemoverRepository.findById(element.getId())
                        .orElseThrow(() -> new NoSuchResourceException("Remover " + element.getId()));
            }
            timeRemover.setHard(true);
            timeRemover.setTimeToDelete(LocalDate.now().plusDays(6).toEpochDay());
            timeRemoverRepository.save(timeRemover);
            utiliseElement(element);
            return Optional.of(element.getKanbanColumn());
        } else {
            return Optional.empty();
        }
    }

    public void utiliseElementFromSystem(long id) {
        // при автоматическом перемещении элемента в корзину не происходит удаления timeRemover, поэтому подтягиваем его и только меняем данные
        TimeRemover timeRemover = timeRemoverRepository.findById(id)
                .orElse(new TimeRemover());
        timeRemover.setHard(true);
        timeRemover.setTimeToDelete(LocalDate.now().plusDays(6).toEpochDay());
        timeRemoverRepository.save(timeRemover);
        utiliseElement(elementRepository.findById(id).get());
    }

    private void utiliseElement(KanbanElement element) {
        if (element.getStatus() == ElementStatus.UTILISE)
            throw new IncorrectStatusException();

        element.setTimeOfUpdate(getEpochSeconds());
        element.setStatus(ElementStatus.UTILISE);
        KanbanColumn column = elementRepository.save(element).getKanbanColumn();

        column.getElements().stream()
                .filter(e -> e.getSerialNumber() > element.getSerialNumber())
                .forEach(e -> e.setSerialNumber(e.getSerialNumber() - 1));
        elementRepository.saveAll(column.getElements());
    }

    public Optional<KanbanElement> getContentFromElement(long id, String userLogin) {
        KanbanElement kanbanElement = elementRepository.findById(id).get();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = kanbanElement.getKanbanColumn().getKanban();
        if (kanban.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))))) {
            return Optional.of(kanbanElement);
        }
        return Optional.empty();
    }

    private long getEpochSeconds() {
        return LocalDateTime.now().toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
    }

    private void createSoftRemover(KanbanColumn column, KanbanElement element) {
        if (column.getDelayedDays() != 0) {
            TimeRemover remover = new TimeRemover();
            remover.setHard(false);
            remover.setRemoverId(element.getId());
            remover.setTimeToDelete(LocalDate.now().plusDays(column.getDelayedDays()).toEpochDay());
            timeRemoverRepository.save(remover);
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
