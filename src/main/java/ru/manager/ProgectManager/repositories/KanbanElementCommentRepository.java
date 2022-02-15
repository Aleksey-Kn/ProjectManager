package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.KanbanElementComment;

public interface KanbanElementCommentRepository extends CrudRepository<KanbanElementComment, Long> {
}
