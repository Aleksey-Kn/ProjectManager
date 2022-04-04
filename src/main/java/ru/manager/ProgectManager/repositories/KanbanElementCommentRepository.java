package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.kanban.KanbanElementComment;

public interface KanbanElementCommentRepository extends CrudRepository<KanbanElementComment, Long> {
}
