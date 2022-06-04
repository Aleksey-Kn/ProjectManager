package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.ScheduledMailInfo;

public interface ScheduledMailInfoRepository extends CrudRepository<ScheduledMailInfo, String> {
}
