package ru.manager.ProgectManager.services.kanban;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.kanban.CreateKanbanElementRequest;
import ru.manager.ProgectManager.DTO.request.kanban.TransportElementRequest;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanElementContentResponse;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.kanban.*;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.ElementStatus;
import ru.manager.ProgectManager.enums.SearchElementType;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.IncorrectStatusException;
import ru.manager.ProgectManager.exception.NoSuchResourceException;
import ru.manager.ProgectManager.repositories.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Log
public class KanbanElementService {
    private final KanbanColumnRepository columnRepository;
    private final KanbanElementRepository elementRepository;
    private final UserRepository userRepository;
    private final TimeRemoverRepository timeRemoverRepository;
    private final KanbanRepository kanbanRepository;

    public Optional<KanbanElement> addElement(CreateKanbanElementRequest request, String userLogin) {
        KanbanColumn column = columnRepository.findById(request.getColumnId()).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = column.getKanban();
        if (canEditKanban(kanban, user)) {
            KanbanElement element = new KanbanElement();
            element.setName(request.getName().trim());
            element.setOwner(user);
            element.setLastRedactor(user);
            element.setKanbanColumn(column);
            element.setStatus(ElementStatus.ALIVE);
            element.setTimeOfCreate(getEpochSeconds());
            element.setTimeOfUpdate(getEpochSeconds());

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

    public boolean rename(long id, String name, String userLogin) {
        KanbanElement element = elementRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(element);
            element.setName(name.trim());
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);
            elementRepository.save(element);
            return true;
        } else {
            return false;
        }
    }

    public boolean editContent(long id, String content, String userLogin) {
        KanbanElement element = elementRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(element);
            element.setContent(content);
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);
            elementRepository.save(element);
            return true;
        } else {
            return false;
        }
    }

    public boolean editDate(long id, String date, String userLogin) {
        log.info("Get date: " + date);
        int oddIndex = date.indexOf('Z');
        if(oddIndex != -1) {
            date = date.substring(0, oddIndex);
        }
        KanbanElement element = elementRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(element);
            element.setSelectedDate(LocalDateTime.parse(date).toEpochSecond(ZoneOffset.ofHours(user.getZoneId())));
            log.info("Save date: " + LocalDateTime.ofEpochSecond(element.getSelectedDate(), 0,
                    ZoneOffset.ofHours(user.getZoneId())));
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);
            elementRepository.save(element);
            return true;
        } else {
            return false;
        }
    }

    public boolean dropDate(long id, String userLogin) {
        KanbanElement element = elementRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(element);
            element.setSelectedDate(0);
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);
            elementRepository.save(element);
            return true;
        } else {
            return false;
        }
    }

    public boolean transportElement(TransportElementRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(request.getId()).orElseThrow();
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
                    element.setLastRedactor(user);

                    elementRepository.saveAll(allElements);
                } else {
                    KanbanColumn toColumn = columnRepository.findById((long) request.getToColumn()).orElseThrow();
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
                    element.setLastRedactor(user);

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
        KanbanElement element = elementRepository.findById(id).orElseThrow();
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

            element.setLastRedactor(user);
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
        utiliseElement(elementRepository.findById(id).orElseThrow());
    }

    private void utiliseElement(KanbanElement element) {
        checkElement(element);

        element.setTimeOfUpdate(getEpochSeconds());
        element.setStatus(ElementStatus.UTILISE);
        KanbanColumn column = elementRepository.save(element).getKanbanColumn();

        column.getElements().stream()
                .filter(e -> e.getSerialNumber() > element.getSerialNumber())
                .forEach(e -> e.setSerialNumber(e.getSerialNumber() - 1));
        elementRepository.saveAll(column.getElements());
    }

    public Optional<KanbanElementContentResponse> getContentFromElement(long id, String userLogin) {
        KanbanElement kanbanElement = elementRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = kanbanElement.getKanbanColumn().getKanban();
        if (canSeeKanban(kanban, user)) {
            return Optional.of(new KanbanElementContentResponse(kanbanElement, user.getZoneId(),
                    canEditKanban(kanban, user)));
        }
        return Optional.empty();
    }

    public Optional<Set<KanbanElement>> findElements(long kanbanId, SearchElementType type, String inputName,
                                                     ElementStatus from, String userLogin){
        Kanban kanban = kanbanRepository.findById(kanbanId).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        String name = inputName.trim().toLowerCase();
        if(canSeeKanban(kanban, user)){
            if(type == SearchElementType.NAME){
                return Optional.of(findByName(kanban, name, from));
            } else if(type == SearchElementType.TAG){
                return Optional.of(findByTag(kanban, name, from));
            } else {
                Set<KanbanElement> set = new HashSet<>();
                set.addAll(findByName(kanban, name, from));
                set.addAll(findByTag(kanban, name, from));
                return Optional.of(set);
            }
        } else{
            return Optional.empty();
        }
    }

    private Set<KanbanElement> findByName(Kanban kanban, String name, ElementStatus elementStatus){
        return kanban.getKanbanColumns().stream()
                .flatMap(c -> c.getElements().stream())
                .filter(e -> e.getStatus() == elementStatus)
                .filter(e -> e.getName().toLowerCase().contains(name))
                .collect(Collectors.toSet());
    }

    private Set<KanbanElement> findByTag(Kanban kanban, String tagName, ElementStatus elementStatus){
        return kanban.getKanbanColumns().stream()
                .flatMap(c -> c.getElements().stream())
                .filter(e -> e.getStatus() == elementStatus)
                .filter(e -> e.getTags().stream().map(Tag::getText).map(String::toLowerCase)
                        .anyMatch(s -> s.contains(tagName)))
                .collect(Collectors.toSet());
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

    private boolean canSeeKanban(Kanban kanban, User user){
        return kanban.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))));
    }

    private boolean canEditKanban(Kanban kanban, User user){
        return kanban.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                .filter(CustomRoleWithKanbanConnector::isCanEdit)
                .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))));
    }

    private void checkElement(KanbanElement element) {
        if(element.getStatus() == ElementStatus.UTILISE)
            throw new IncorrectStatusException();
        if(element.getStatus() == ElementStatus.DELETED)
            throw new NoSuchElementException();
    }
}
