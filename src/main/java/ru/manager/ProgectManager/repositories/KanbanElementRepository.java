package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;

public interface KanbanElementRepository extends CrudRepository<KanbanElement, Long> {
}
