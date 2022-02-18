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
import ru.manager.ProgectManager.repositories.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArchiveAndTrashService {
    private final UserRepository userRepository;
    private final KanbanColumnRepository columnRepository;
    private final KanbanElementRepository elementRepository;

    public Optional<KanbanColumn> finalDeleteElementFromTrash(long id, String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        KanbanElement element = elementRepository.findById(id).get();
        if (element.getKanbanColumn().getKanban().getProject().getConnectors().stream()
                .anyMatch(c -> c.getUser().equals(user))) {
            if(element.getStatus() != ElementStatus.UTILISE)
                throw new IncorrectStatusException();

            KanbanColumn column = element.getKanbanColumn();
            column.getElements().remove(element);
            return Optional.of(columnRepository.save(column));
        } else {
            return Optional.empty();
        }
    }
}
