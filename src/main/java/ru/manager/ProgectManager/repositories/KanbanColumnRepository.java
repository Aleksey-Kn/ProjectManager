package ru.manager.ProgectManager.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.KanbanColumn;

import java.util.Optional;

public interface KanbanColumnRepository extends CrudRepository<KanbanColumn, Long> {
    @Query(nativeQuery = true, value = "select max(serial_number) from project_manager.kanban_column")
    Optional<Integer> findLastSerialNumber();
}
