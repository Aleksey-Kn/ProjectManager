package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.entitys.KanbanColumn;
import ru.manager.ProgectManager.entitys.KanbanElement;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.enums.ElementStatus;
import ru.manager.ProgectManager.exception.IncorrectStatusException;
import ru.manager.ProgectManager.repositories.KanbanColumnRepository;
import ru.manager.ProgectManager.repositories.KanbanElementRepository;
import ru.manager.ProgectManager.repositories.TimeRemoverRepository;
import ru.manager.ProgectManager.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class ArchiveAndTrashService {
    private final UserRepository userRepository;
    private final KanbanColumnRepository columnRepository;
    private final KanbanElementRepository elementRepository;
    private final TimeRemoverRepository timeRemoverRepository;

    public void finalDeleteElementFromTrash(long id) {
        KanbanElement element = elementRepository.findById(id).get();
        if (element.getStatus() != ElementStatus.UTILISE)
            throw new IncorrectStatusException();

        KanbanColumn column = element.getKanbanColumn();
        column.getElements().remove(element);
        columnRepository.save(column);
    }

    public boolean archive(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(id).get();
        if (element.getKanbanColumn().getKanban().getProject().getConnectors().stream()
                .anyMatch(c -> c.getUser().equals(user))) {
            if (element.getStatus() == ElementStatus.ARCHIVED)
                throw new IncorrectStatusException();

            timeRemoverRepository.findById(id).ifPresent(timeRemoverRepository::delete);

            element.setStatus(ElementStatus.ARCHIVED);
            KanbanColumn column = elementRepository.save(element).getKanbanColumn();
            column.getElements().stream()
                    .filter(e -> e.getSerialNumber() > element.getSerialNumber())
                    .forEach(e -> e.setSerialNumber(e.getSerialNumber() - 1));
            columnRepository.save(column);
            return true;
        } else {
            return false;
        }
    }

    public boolean reestablish(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(id).get();
        if (element.getKanbanColumn().getKanban().getProject().getConnectors().stream()
                .anyMatch(c -> c.getUser().equals(user))) {
            if (element.getStatus() == ElementStatus.ALIVE)
                throw new IncorrectStatusException();

            timeRemoverRepository.findById(id).ifPresent(timeRemoverRepository::delete);

            element.setStatus(ElementStatus.ALIVE);
            element.setSerialNumber(element.getKanbanColumn().getElements().stream()
                    .filter(e -> e.getStatus() == ElementStatus.ALIVE)
                    .mapToInt(KanbanElement::getSerialNumber)
                    .max().orElse(-1) + 1);
            elementRepository.save(element);
            return true;
        } else {
            return false;
        }
    }
}
