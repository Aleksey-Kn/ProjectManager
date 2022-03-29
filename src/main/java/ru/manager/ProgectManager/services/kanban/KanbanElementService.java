package ru.manager.ProgectManager.services.kanban;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.manager.ProgectManager.DTO.request.CreateKanbanElementRequest;
import ru.manager.ProgectManager.DTO.request.KanbanCommentRequest;
import ru.manager.ProgectManager.DTO.request.TransportElementRequest;
import ru.manager.ProgectManager.DTO.request.UpdateKanbanElementRequest;
import ru.manager.ProgectManager.entitys.*;
import ru.manager.ProgectManager.enums.ElementStatus;
import ru.manager.ProgectManager.exception.IncorrectStatusException;
import ru.manager.ProgectManager.repositories.*;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class KanbanElementService {
    private final KanbanColumnRepository columnRepository;
    private final KanbanElementRepository elementRepository;
    private final UserRepository userRepository;
    private final KanbanElementCommentRepository commentRepository;
    private final KanbanAttachmentRepository attachmentRepository;
    private final TimeRemoverRepository timeRemoverRepository;

    public Optional<KanbanElement> addElement(CreateKanbanElementRequest request, String userLogin) {
        KanbanColumn column = columnRepository.findById(request.getColumnId()).get();
        User user = userRepository.findByUsername(userLogin);
        if (column.getKanban().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))) {
            KanbanElement element = new KanbanElement();
            element.setContent(request.getContent());
            element.setName(request.getName());
            element.setTag(request.getTag());
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

    public Optional<KanbanElement> setElement(long id, UpdateKanbanElementRequest request, String userLogin) {
        KanbanElement element = elementRepository.findById(id).get();
        User user = userRepository.findByUsername(userLogin);
        if (element.getKanbanColumn().getKanban().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))) {
            if (element.getStatus() == ElementStatus.UTILISE)
                throw new IncorrectStatusException();
            element.setContent(request.getContent());
            element.setName(request.getName());
            element.setTag(request.getTag());
            element.setTimeOfUpdate(getEpochSeconds());

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
        if (fromColumn.getKanban().getProject().getConnectors().stream()
                .anyMatch(c -> c.getUser().equals(user))) {
            if (element.getStatus() == ElementStatus.ALIVE) {
                if (fromColumn.getId() == request.getToColumn()) {
                    List<KanbanElement> allElements = fromColumn.getElements();
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
                    List<KanbanElement> fromColumnElements = fromColumn.getElements();
                    List<KanbanElement> toColumnElements = toColumn.getElements();
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

                    if(fromColumn.getDelayedDays() != 0){
                        timeRemoverRepository.deleteById(element.getId());
                    }
                    createSoftRemover(toColumn, element);

                    elementRepository.saveAll(fromColumnElements);
                    elementRepository.saveAll(toColumnElements);
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
        if (element.getKanbanColumn().getKanban().getProject().getConnectors().stream()
                .anyMatch(c -> c.getUser().equals(user))) {
            TimeRemover timeRemover;
            if(element.getKanbanColumn().getDelayedDays() == 0){
                timeRemover = new TimeRemover();
                timeRemover.setRemoverId(element.getId());
            } else{ // если пользователь улаляет элемент из колонки с регулярным удалением вручную, то удалитель уже есть
                timeRemover = timeRemoverRepository.findById(element.getId()).orElseThrow(NullPointerException::new);
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

    public void utiliseElementFromSystem(long id){
        // при автоматическом перемещении элемента в корзину не происходит удаления timeRemover, поэтому подтягиваем его и только меняем данные
        TimeRemover timeRemover = timeRemoverRepository.findById(id).orElseThrow(NullPointerException::new);
        timeRemover.setHard(true);
        timeRemover.setTimeToDelete(LocalDate.now().plusDays(6).toEpochDay());
        timeRemoverRepository.save(timeRemover);
        utiliseElement(elementRepository.findById(id).get());
    }

    private void utiliseElement(KanbanElement element){
        if(element.getStatus() == ElementStatus.UTILISE)
            throw new IncorrectStatusException();

        element.setTimeOfUpdate(getEpochSeconds());
        element.setStatus(ElementStatus.UTILISE);
        KanbanColumn column = elementRepository.save(element).getKanbanColumn();

        column.getElements().stream()
                .filter(e -> e.getSerialNumber() > element.getSerialNumber())
                .forEach(e -> e.setSerialNumber(e.getSerialNumber() - 1));
        elementRepository.saveAll(column.getElements());
    }

    public Optional<KanbanElementComment> addComment(KanbanCommentRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(request.getId()).get();
        if (element.getKanbanColumn().getKanban().getProject().getConnectors().stream()
                .anyMatch(c -> c.getUser().equals(user))) {
            if (element.getStatus() == ElementStatus.UTILISE)
                throw new IncorrectStatusException();
            KanbanElementComment comment = new KanbanElementComment();
            comment.setText(request.getText());
            comment.setOwner(user);
            comment.setKanbanElement(element);
            comment.setDateTime(getEpochSeconds());
            comment = commentRepository.save(comment);

            element.setTimeOfUpdate(getEpochSeconds());
            element.getComments().add(comment);
            elementRepository.save(element);
            return Optional.of(comment);
        } else {
            return Optional.empty();
        }
    }

    public Optional<KanbanElement> deleteComment(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanElementComment comment = commentRepository.findById(id).get();
        if (comment.getOwner().equals(user)
                || comment.getKanbanElement().getKanbanColumn().getKanban().getProject().getConnectors().stream()
                .filter(UserWithProjectConnector::isAdmin)
                .anyMatch(c -> c.getUser().equals(user))) {
            if(comment.getKanbanElement().getStatus() == ElementStatus.UTILISE)
                throw new IncorrectStatusException();
            KanbanElement element = comment.getKanbanElement();
            element.setTimeOfUpdate(getEpochSeconds());
            element.getComments().remove(comment);
            element = elementRepository.save(element);
            return Optional.of(element);
        } else {
            return Optional.empty();
        }
    }

    public Optional<KanbanElementComment> updateComment(KanbanCommentRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanElementComment comment = commentRepository.findById(request.getId()).get();
        if (comment.getOwner().equals(user)) {
            if(comment.getKanbanElement().getStatus() == ElementStatus.UTILISE)
                throw new IncorrectStatusException();
            comment.getKanbanElement().setTimeOfUpdate(getEpochSeconds());
            comment.setText(request.getText());
            comment.setDateTime(getEpochSeconds());
            return Optional.of(commentRepository.save(comment));
        } else {
            return Optional.empty();
        }
    }

    public Optional<KanbanElement> addAttachment(long id, String userLogin, MultipartFile file) throws IOException {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(id).get();
        if (element.getKanbanColumn().getKanban().getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user))) {
            if (element.getStatus() == ElementStatus.UTILISE)
                throw new IncorrectStatusException();
            KanbanAttachment attachment = new KanbanAttachment();
            attachment.setFileData(file.getBytes());
            attachment.setFilename(file.getOriginalFilename());
            attachment.setElement(element);
            attachment = attachmentRepository.save(attachment);

            element.setTimeOfUpdate(getEpochSeconds());
            element.getKanbanAttachments().add(attachment);
            return Optional.of(elementRepository.save(element));
        }
        return Optional.empty();
    }

    public Optional<KanbanAttachment> getAttachment(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanAttachment attachment = attachmentRepository.findById(id).get();
        if (attachment.getElement().getKanbanColumn().getKanban().getProject().getConnectors().stream()
                .anyMatch(c -> c.getUser().equals(user))) {
            return Optional.of(attachment);
        } else {
            return Optional.empty();
        }
    }

    public Optional<KanbanElement> deleteAttachment(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanAttachment attachment = attachmentRepository.findById(id).get();
        if (attachment.getElement().getKanbanColumn().getKanban().getProject().getConnectors().stream()
                .anyMatch(c -> c.getUser().equals(user))) {
            if (attachment.getElement().getStatus() == ElementStatus.UTILISE)
                throw new IncorrectStatusException();
            KanbanElement element = attachment.getElement();
            element.setTimeOfUpdate(getEpochSeconds());
            element.getKanbanAttachments().remove(attachment);
            return Optional.of(elementRepository.save(element));
        } else {
            return Optional.empty();
        }
    }

    public Optional<KanbanElement> getContentFromElement(long id, String userLogin) {
        KanbanElement kanbanElement = elementRepository.findById(id).get();
        User user = userRepository.findByUsername(userLogin);
        if (kanbanElement
                .getKanbanColumn()
                .getKanban()
                .getProject()
                .getConnectors().stream().anyMatch(c -> c.getUser().equals(user))) {
            return Optional.of(kanbanElement);
        }
        return Optional.empty();
    }

    public Kanban findKanbanFromElement(long id) {
        return elementRepository.findById(id).get().getKanbanColumn().getKanban();
    }

    private long getEpochSeconds() {
        return LocalDateTime.now().toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
    }

    private void createSoftRemover(KanbanColumn column, KanbanElement element) {
        if(column.getDelayedDays() != 0){
            TimeRemover remover = new TimeRemover();
            remover.setHard(false);
            remover.setRemoverId(element.getId());
            remover.setTimeToDelete(LocalDate.now().plusDays(column.getDelayedDays()).toEpochDay());
            timeRemoverRepository.save(remover);
        }
    }
}
