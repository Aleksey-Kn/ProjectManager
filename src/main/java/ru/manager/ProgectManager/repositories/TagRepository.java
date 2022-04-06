package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.kanban.Tag;

public interface TagRepository extends CrudRepository<Tag, Long> {
}
