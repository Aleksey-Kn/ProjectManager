package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.CustomRoleWithKanbanConnector;

public interface KanbanConnectorRepository extends CrudRepository<CustomRoleWithKanbanConnector, Long> {
}
