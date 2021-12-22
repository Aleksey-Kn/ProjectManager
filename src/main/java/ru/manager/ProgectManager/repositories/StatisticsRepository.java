package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.StatisticsUsing;

public interface StatisticsRepository extends CrudRepository<StatisticsUsing, Long> {
    StatisticsUsing findByType(String type);
}
