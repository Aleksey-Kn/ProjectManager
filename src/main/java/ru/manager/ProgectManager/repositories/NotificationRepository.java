package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.user.Notification;

public interface NotificationRepository extends CrudRepository<Notification, Long> {
}
