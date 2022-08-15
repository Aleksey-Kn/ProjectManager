package ru.manager.ProgectManager.services.kanban;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.manager.ProgectManager.DTO.request.kanban.CheckboxRequest;
import ru.manager.ProgectManager.DTO.request.kanban.KanbanCommentRequest;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.kanban.*;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.ElementStatus;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.kanban.IncorrectElementStatusException;
import ru.manager.ProgectManager.exception.kanban.NoSuchKanbanElementException;
import ru.manager.ProgectManager.exception.kanban.NoSuchTagException;
import ru.manager.ProgectManager.repositories.*;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KanbanElementAttributesService {
    private final KanbanElementRepository elementRepository;
    private final UserRepository userRepository;
    private final KanbanElementCommentRepository commentRepository;
    private final KanbanAttachmentRepository attachmentRepository;
    private final CheckboxRepository checkboxRepository;

    public Optional<KanbanElementComment> addComment(KanbanCommentRequest request, String userLogin)
            throws IncorrectElementStatusException, NoSuchKanbanElementException {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(request.getId()).orElseThrow();
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canSeeKanban(kanban, user)) {
            checkElement(element);
            KanbanElementComment comment = new KanbanElementComment();
            comment.setText(request.getText());
            comment.setOwner(user);
            comment.setKanbanElement(element);
            comment.setDateTime(getEpochSeconds());
            comment = commentRepository.save(comment);

            element.setLastRedactor(user);
            element.setTimeOfUpdate(getEpochSeconds());
            element.getComments().add(comment);
            elementRepository.save(element);
            return Optional.of(comment);
        } else {
            return Optional.empty();
        }
    }

    public Optional<KanbanElement> deleteComment(long id, String userLogin)
            throws IncorrectElementStatusException, NoSuchKanbanElementException {
        User user = userRepository.findByUsername(userLogin);
        KanbanElementComment comment = commentRepository.findById(id).orElseThrow();
        if (comment.getOwner().equals(user)
                || comment.getKanbanElement().getKanbanColumn().getKanban().getProject().getConnectors().stream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getUser().equals(user))) {
            checkElement(comment.getKanbanElement());
            KanbanElement element = comment.getKanbanElement();
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);
            element.getComments().remove(comment);
            element = elementRepository.save(element);
            return Optional.of(element);
        } else {
            return Optional.empty();
        }
    }

    public Optional<KanbanElementComment> updateComment(KanbanCommentRequest request, String userLogin) throws IncorrectElementStatusException, NoSuchKanbanElementException {
        User user = userRepository.findByUsername(userLogin);
        KanbanElementComment comment = commentRepository.findById(request.getId()).orElseThrow();
        if (comment.getOwner().equals(user)) {
            KanbanElement element = comment.getKanbanElement();
            checkElement(element);
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);
            elementRepository.save(element);

            comment.setText(request.getText());
            comment.setDateTime(getEpochSeconds());
            comment.setRedacted(true);
            return Optional.of(commentRepository.save(comment));
        } else {
            return Optional.empty();
        }
    }

    public Optional<KanbanAttachment> addAttachment(long id, String userLogin, MultipartFile file)
            throws IOException, IncorrectElementStatusException, NoSuchKanbanElementException {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(id).orElseThrow();
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(element);
            KanbanAttachment attachment = new KanbanAttachment();
            attachment.setFileData(file.getBytes());
            attachment.setFilename(file.getOriginalFilename());
            attachment.setElement(element);
            attachment = attachmentRepository.save(attachment);

            element.setLastRedactor(user);
            element.setTimeOfUpdate(getEpochSeconds());
            element.getKanbanAttachments().add(attachment);
            elementRepository.save(element);
            return Optional.of(attachment);
        } else {
            return Optional.empty();
        }
    }

    public Optional<KanbanAttachment> getAttachment(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanAttachment attachment = attachmentRepository.findById(id).orElseThrow();
        Kanban kanban = attachment.getElement().getKanbanColumn().getKanban();
        if(attachment.getElement().getStatus() == ElementStatus.DELETED)
            throw new NoSuchElementException();
        if (canSeeKanban(kanban, user)) {
            return Optional.of(attachment);
        } else {
            return Optional.empty();
        }
    }

    public Optional<KanbanElement> deleteAttachment(long id, String userLogin) throws IncorrectElementStatusException, NoSuchKanbanElementException {
        User user = userRepository.findByUsername(userLogin);
        KanbanAttachment attachment = attachmentRepository.findById(id).orElseThrow();
        Kanban kanban = attachment.getElement().getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            KanbanElement element = attachment.getElement();
            checkElement(element);
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);

            element.getKanbanAttachments().remove(attachment);
            return Optional.of(elementRepository.save(element));
        } else {
            return Optional.empty();
        }
    }

    public void addTag(long elementId, long tagId, String userLogin)
            throws ForbiddenException, NoSuchTagException, NoSuchKanbanElementException, IncorrectElementStatusException {
        KanbanElement element = elementRepository.findById(elementId).orElseThrow(NoSuchKanbanElementException::new);
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(element);
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);

            element.getTags().add(element.getKanbanColumn().getKanban().getAvailableTags().stream()
                    .filter(t -> t.getId() == tagId)
                    .findAny().orElseThrow(NoSuchTagException::new));
            elementRepository.save(element);
        } else{
            throw new ForbiddenException();
        }
    }

    public void removeTag(long elementId, long tagId, String userLogin)
            throws IncorrectElementStatusException, NoSuchKanbanElementException, ForbiddenException {
        KanbanElement element = elementRepository.findById(elementId).orElseThrow(NoSuchKanbanElementException::new);
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(element);
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);

            element.getTags().removeIf(tag -> tag.getId() == tagId);
            elementRepository.save(element);
        } else {
            throw new ForbiddenException();
        }
    }

    public Optional<CheckBox> addCheckbox(CheckboxRequest request, String userLogin) throws IncorrectElementStatusException, NoSuchKanbanElementException {
        KanbanElement element = elementRepository.findById(request.getElementId()).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(element);
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);

            CheckBox checkBox = new CheckBox();
            checkBox.setCheck(false);
            checkBox.setText(request.getText());
            checkBox.setElement(element);
            checkBox = checkboxRepository.save(checkBox);
            element.getCheckBoxes().add(checkBox);
            elementRepository.save(element);
            return Optional.of(checkBox);
        } else{
            return Optional.empty();
        }
    }

    public boolean deleteCheckbox(long id, String userLogin)
            throws IncorrectElementStatusException, NoSuchKanbanElementException {
        CheckBox checkBox = checkboxRepository.findById(id).orElseThrow();
        KanbanElement element = checkBox.getElement();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(element);
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);

            element.getCheckBoxes().remove(checkBox);
            elementRepository.save(element);
            return true;
        } else {
            return false;
        }
    }

    public boolean tapCheckbox(long id, String userLogin)
            throws IncorrectElementStatusException, NoSuchKanbanElementException {
        CheckBox checkBox = checkboxRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = checkBox.getElement().getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            KanbanElement element = checkBox.getElement();
            checkElement(element);
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);
            elementRepository.save(element);

            checkBox.setCheck(!checkBox.isCheck());
            checkboxRepository.save(checkBox);
            return true;
        } else {
            return false;
        }
    }

    public boolean editCheckbox(long id, String newText, String userLogin)
            throws IncorrectElementStatusException, NoSuchKanbanElementException {
        CheckBox checkBox = checkboxRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = checkBox.getElement().getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            KanbanElement element = checkBox.getElement();
            checkElement(element);
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);
            elementRepository.save(element);

            checkBox.setText(newText);
            checkboxRepository.save(checkBox);
            return true;
        } else{
            return false;
        }
    }

    private long getEpochSeconds() {
        return LocalDateTime.now().toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
    }

    private boolean canEditKanban(Kanban kanban, User user){
        return kanban.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                .filter(CustomRoleWithKanbanConnector::isCanEdit)
                .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))));
    }

    private boolean canSeeKanban(Kanban kanban, User user) {
        return kanban.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))));
    }

    private void checkElement(KanbanElement element)
            throws IncorrectElementStatusException, NoSuchKanbanElementException {
        if(element.getStatus() == ElementStatus.UTILISE)
            throw new IncorrectElementStatusException();
        if(element.getStatus() == ElementStatus.DELETED)
            throw new NoSuchKanbanElementException();
    }
}
