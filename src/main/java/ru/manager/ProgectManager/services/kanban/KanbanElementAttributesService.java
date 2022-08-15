package ru.manager.ProgectManager.services.kanban;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.manager.ProgectManager.DTO.request.kanban.CheckboxRequest;
import ru.manager.ProgectManager.DTO.request.kanban.KanbanCommentRequest;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.DTO.response.kanban.AttachAllDataResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanElementCommentResponse;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithKanbanConnector;
import ru.manager.ProgectManager.entitys.kanban.*;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.ElementStatus;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.kanban.*;
import ru.manager.ProgectManager.repositories.*;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class KanbanElementAttributesService {
    private final KanbanElementRepository elementRepository;
    private final UserRepository userRepository;
    private final KanbanElementCommentRepository commentRepository;
    private final KanbanAttachmentRepository attachmentRepository;
    private final CheckboxRepository checkboxRepository;

    @Transactional
    public IdResponse addComment(KanbanCommentRequest request, String userLogin)
            throws IncorrectElementStatusException, NoSuchKanbanElementException, ForbiddenException {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(request.getId())
                .orElseThrow(NoSuchKanbanElementException::new);
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
            return new IdResponse(comment.getId());
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void deleteComment(long id, String userLogin)
            throws IncorrectElementStatusException, NoSuchKanbanElementException, ForbiddenException, NoSuchCommentException {
        User user = userRepository.findByUsername(userLogin);
        KanbanElementComment comment = commentRepository.findById(id).orElseThrow(NoSuchCommentException::new);
        if (comment.getOwner().equals(user)
                || comment.getKanbanElement().getKanbanColumn().getKanban().getProject().getConnectors().stream()
                .filter(c -> c.getRoleType() == TypeRoleProject.ADMIN)
                .anyMatch(c -> c.getUser().equals(user))) {
            checkElement(comment.getKanbanElement());
            KanbanElement element = comment.getKanbanElement();
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);
            element.getComments().remove(comment);
            elementRepository.save(element);
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public KanbanElementCommentResponse updateComment(KanbanCommentRequest request, String userLogin)
            throws IncorrectElementStatusException, NoSuchKanbanElementException, ForbiddenException, NoSuchCommentException {
        User user = userRepository.findByUsername(userLogin);
        KanbanElementComment comment = commentRepository.findById(request.getId())
                .orElseThrow(NoSuchCommentException::new);
        if (comment.getOwner().equals(user)) {
            int zoneId = user.getZoneId();
            KanbanElement element = comment.getKanbanElement();
            checkElement(element);
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);
            elementRepository.save(element);

            comment.setText(request.getText());
            comment.setDateTime(getEpochSeconds());
            comment.setRedacted(true);
            return new KanbanElementCommentResponse(commentRepository.save(comment), zoneId);
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public IdResponse addAttachment(long id, String userLogin, MultipartFile file)
            throws IOException, IncorrectElementStatusException, NoSuchKanbanElementException, ForbiddenException {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(id).orElseThrow(NoSuchKanbanElementException::new);
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
            return new IdResponse(attachment.getId());
        } else {
            throw new ForbiddenException();
        }
    }

    public AttachAllDataResponse getAttachment(long id, String userLogin)
            throws ForbiddenException, NoSuchAttachmentException, NoSuchKanbanElementException {
        User user = userRepository.findByUsername(userLogin);
        KanbanAttachment attachment = attachmentRepository.findById(id).orElseThrow(NoSuchAttachmentException::new);
        Kanban kanban = attachment.getElement().getKanbanColumn().getKanban();
        if(attachment.getElement().getStatus() == ElementStatus.DELETED)
            throw new NoSuchKanbanElementException();
        if (canSeeKanban(kanban, user)) {
            return new AttachAllDataResponse(attachment);
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void deleteAttachment(long id, String userLogin)
            throws IncorrectElementStatusException, NoSuchKanbanElementException, NoSuchAttachmentException, ForbiddenException {
        User user = userRepository.findByUsername(userLogin);
        KanbanAttachment attachment = attachmentRepository.findById(id).orElseThrow(NoSuchAttachmentException::new);
        Kanban kanban = attachment.getElement().getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            KanbanElement element = attachment.getElement();
            checkElement(element);
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);

            element.getKanbanAttachments().remove(attachment);
            elementRepository.save(element);
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
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

    @Transactional
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

    @Transactional
    public IdResponse addCheckbox(CheckboxRequest request, String userLogin) throws IncorrectElementStatusException, NoSuchKanbanElementException, ForbiddenException {
        KanbanElement element = elementRepository.findById(request.getElementId())
                .orElseThrow(NoSuchKanbanElementException::new);
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
            return new IdResponse(checkBox.getId());
        } else{
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void deleteCheckbox(long id, String userLogin)
            throws IncorrectElementStatusException, NoSuchKanbanElementException, NoSuchCheckboxException, ForbiddenException {
        CheckBox checkBox = checkboxRepository.findById(id).orElseThrow(NoSuchCheckboxException::new);
        KanbanElement element = checkBox.getElement();
        User user = userRepository.findByUsername(userLogin);
        Kanban kanban = element.getKanbanColumn().getKanban();
        if (canEditKanban(kanban, user)) {
            checkElement(element);
            element.setTimeOfUpdate(getEpochSeconds());
            element.setLastRedactor(user);

            element.getCheckBoxes().remove(checkBox);
            elementRepository.save(element);
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void tapCheckbox(long id, String userLogin)
            throws IncorrectElementStatusException, NoSuchKanbanElementException, NoSuchCheckboxException, ForbiddenException {
        CheckBox checkBox = checkboxRepository.findById(id).orElseThrow(NoSuchCheckboxException::new);
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
        } else {
            throw new ForbiddenException();
        }
    }

    @Transactional
    public void editCheckbox(long id, String newText, String userLogin)
            throws IncorrectElementStatusException, NoSuchKanbanElementException, ForbiddenException, NoSuchCheckboxException {
        CheckBox checkBox = checkboxRepository.findById(id).orElseThrow(NoSuchCheckboxException::new);
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
        } else{
            throw new ForbiddenException();
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
