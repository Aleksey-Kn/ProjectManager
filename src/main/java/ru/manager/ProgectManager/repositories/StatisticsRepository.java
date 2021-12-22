package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.StatisticsUsing;

public interface StatisticsRepository extends CrudRepository<StatisticsUsing, Integer> {
    StatisticsUsing findByType(String type);
}
