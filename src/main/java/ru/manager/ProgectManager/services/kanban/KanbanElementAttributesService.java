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
import ru.manager.ProgectManager.exception.IncorrectStatusException;
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

    public Optional<KanbanElementComment> addComment(KanbanCommentRequest request, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(request.getId()).orElseThrow();
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(element);
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
        KanbanElementComment comment = commentRepository.findById(id).orElseThrow();
        if (comment.getOwner().equals(user)
                || comment.getKanbanElement().getKanbanColumn().getKanban().getProject().getConnectors().stream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getUser().equals(user))) {
            checkElement(comment.getKanbanElement());
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
        KanbanElementComment comment = commentRepository.findById(request.getId()).orElseThrow();
        if (comment.getOwner().equals(user)) {
            checkElement(comment.getKanbanElement());
            comment.getKanbanElement().setTimeOfUpdate(getEpochSeconds());
            comment.setText(request.getText());
            comment.setDateTime(getEpochSeconds());
            return Optional.of(commentRepository.save(comment));
        } else {
            return Optional.empty();
        }
    }

    public Optional<KanbanAttachment> addAttachment(long id, String userLogin, MultipartFile file) throws IOException {
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
        if (kanban.getProject().getConnectors().stream().anyMatch(c -> c.getUser().equals(user)
                && (c.getRoleType() != TypeRoleProject.CUSTOM_ROLE
                || c.getCustomProjectRole().getCustomRoleWithKanbanConnectors().stream()
                .anyMatch(kanbanConnector -> kanbanConnector.getKanban().equals(kanban))))) {
            return Optional.of(attachment);
        } else {
            return Optional.empty();
        }
    }

    public Optional<KanbanElement> deleteAttachment(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanAttachment attachment = attachmentRepository.findById(id).orElseThrow();
        Kanban kanban = attachment.getElement().getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(attachment.getElement());
            KanbanElement element = attachment.getElement();
            element.setTimeOfUpdate(getEpochSeconds());
            element.getKanbanAttachments().remove(attachment);
            return Optional.of(elementRepository.save(element));
        } else {
            return Optional.empty();
        }
    }

    public boolean addTag(long elementId, long tagId, String userLogin){
        KanbanElement element = elementRepository.findById(elementId).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(element);
            element.getTags().add(element.getKanbanColumn().getKanban().getAvailableTags().stream()
                    .filter(t -> t.getId() == tagId)
                    .findAny().orElseThrow(IllegalArgumentException::new));
            elementRepository.save(element);
            return true;
        } else{
            return false;
        }
    }

    public boolean removeTag(long elementId, long tagId, String userLogin){
        KanbanElement element = elementRepository.findById(elementId).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(element);
            element.getTags().removeIf(tag -> tag.getId() == tagId);
            elementRepository.save(element);
            return true;
        } else {
            return false;
        }
    }

    public Optional<CheckBox> addCheckbox(CheckboxRequest request, String userLogin){
        KanbanElement element = elementRepository.findById(request.getElementId()).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(element);
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

    public boolean deleteCheckbox(long id, String userLogin){
        CheckBox checkBox = checkboxRepository.findById(id).orElseThrow();
        KanbanElement element = checkBox.getElement();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(element);
            element.getCheckBoxes().remove(checkBox);
            elementRepository.save(element);
            return true;
        } else {
            return false;
        }
    }

    public boolean tapCheckbox(long id, String userLogin){
        CheckBox checkBox = checkboxRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = checkBox.getElement().getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(checkBox.getElement());
            checkBox.setCheck(!checkBox.isCheck());
            checkboxRepository.save(checkBox);
            return true;
        } else {
            return false;
        }
    }

    public boolean editCheckbox(long id, String newText, String userLogin){
        CheckBox checkBox = checkboxRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = checkBox.getElement().getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(checkBox.getElement());
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

    private void checkElement(KanbanElement element) {
        if(element.getStatus() == ElementStatus.UTILISE)
            throw new IncorrectStatusException();
        if(element.getStatus() == ElementStatus.DELETED)
            throw new NoSuchElementException();
    }
}
