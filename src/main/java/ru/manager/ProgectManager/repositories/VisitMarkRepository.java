package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.user.VisitMark;

public interface VisitMarkRepository extends CrudRepository<VisitMark, Long> {
}
