package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.kanban.CheckBox;

public interface CheckboxRepository extends CrudRepository<CheckBox, Long> {
}
