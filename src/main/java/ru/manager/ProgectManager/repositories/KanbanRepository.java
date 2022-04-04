package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.kanban.Kanban;

public interface KanbanRepository extends CrudRepository<Kanban, Long> {
}
