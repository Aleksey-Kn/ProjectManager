package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.kanban.TimeRemover;

public interface TimeRemoverRepository extends CrudRepository<TimeRemover, Long> {

}
