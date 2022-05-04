package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.user.WorkTrack;

public interface WorkTrackRepository extends CrudRepository<WorkTrack, Long> {
}
