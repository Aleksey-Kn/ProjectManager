package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.entitys.KanbanColumn;
import ru.manager.ProgectManager.entitys.KanbanElement;
import ru.manager.ProgectManager.repositories.ProjectRepository;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;

    public List<KanbanColumn> findKanbans(long id){
        List<KanbanColumn> result = projectRepository.findById(id).get().getKanbanColumns();
        result.sort(Comparator.comparing(KanbanColumn::getSerialNumber));
        for(KanbanColumn column: result){
            column.getElements().sort(Comparator.comparing(KanbanElement::getSerialNumber));
        }
        return result;
    }
}
