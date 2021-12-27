package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.entitys.KanbanElement;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.repositories.KanbanColumnRepository;
import ru.manager.ProgectManager.repositories.KanbanElementRepository;
import ru.manager.ProgectManager.repositories.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KanbanService {
    private final KanbanColumnRepository columnRepository;
    private final KanbanElementRepository elementRepository;
    private final UserRepository userRepository;

    public Optional<String> getContentFromElement(long id, String userLogin){
        KanbanElement kanbanElement = elementRepository.findById(id).get();
        User user = userRepository.findByUsername(userLogin);
        if(kanbanElement
                .getKanbanColumn()
                .getProject()
                .getConnectors().stream().anyMatch(c -> c.getUser().equals(user))){
            return Optional.of(kanbanElement.getContent());
        }
        return Optional.empty();
    }
}
