package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.kanban.KanbanColumn;

public interface KanbanColumnRepository extends CrudRepository<KanbanColumn, Long> {

}
