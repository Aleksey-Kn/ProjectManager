package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.KanbanConnector;

public interface KanbanConnectorRepository extends CrudRepository<KanbanConnector, Long> {
}
