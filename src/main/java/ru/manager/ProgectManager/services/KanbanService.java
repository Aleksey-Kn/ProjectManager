package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.manager.ProgectManager.DTO.request.*;
import ru.manager.ProgectManager.entitys.*;
import ru.manager.ProgectManager.enums.ElementStatus;
import ru.manager.ProgectManager.exception.IncorrectStatusException;
import ru.manager.ProgectManager.repositories.*;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
    private final KanbanElementCommentRepository commentRepository;
    private final KanbanAttachmentRepository attachmentRepository;

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

            element.setLastRedactor(user);
            return Optional.of(elementRepository.save(element));
        }
        return Optional.empty();
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

    public boolean transportElement(TransportElementRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(request.getId()).get();
        int from = element.getSerialNumber();
        if (element.getKanbanColumn().getKanban().getProject().getConnectors().stream()
                .anyMatch(c -> c.getUser().equals(user))) {
            if (element.getStatus() == ElementStatus.ALIVE) {
                if (element.getKanbanColumn().getId() == request.getToColumn()) {
                    List<KanbanElement> allElements = element.getKanbanColumn().getElements();
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
                } else {
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
            } else throw new IncorrectStatusException();
        }
        return false;
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

    public Optional<KanbanColumn> deleteElement(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(id).get();
        if (element.getKanbanColumn().getKanban().getProject().getConnectors().stream()
                .anyMatch(c -> c.getUser().equals(user))) {
            element.setStatus(ElementStatus.UTILISE);
            return Optional.of(elementRepository.save(element).getKanbanColumn());
        } else {
            return Optional.empty();
        }
    }

    public Optional<KanbanColumn> finalDeleteElementFromTrash(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(id).get();
        if (element.getKanbanColumn().getKanban().getProject().getConnectors().stream()
                .anyMatch(c -> c.getUser().equals(user))) {
            if(element.getStatus() != ElementStatus.UTILISE)
                throw new IncorrectStatusException();
            element.getKanbanColumn().getElements().stream()
                    .filter(kanbanElement -> kanbanElement.getSerialNumber() > element.getSerialNumber())
                    .forEach(kanbanElement -> kanbanElement.setSerialNumber(kanbanElement.getSerialNumber() - 1));

            KanbanColumn column = element.getKanbanColumn();
            column.getElements().remove(element);
            return Optional.of(columnRepository.save(column));
        } else {
            return Optional.empty();
        }
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
            comment.setText(request.getText());
            comment.setDateTime(getEpochSeconds());
            return Optional.of(commentRepository.save(comment));
        } else {
            return Optional.empty();
        }
    }

    private long getEpochSeconds() {
        return LocalDateTime.now().toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
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

    public Kanban findKanbanFromElement(long id) {
        return elementRepository.findById(id).get().getKanbanColumn().getKanban();
    }

    public Kanban findKanbanFromColumn(long id) {
        return columnRepository.findById(id).get().getKanban();
    }
}
