package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.accessProject.UserWithProjectConnector;

public interface UserWithProjectConnectorRepository extends CrudRepository<UserWithProjectConnector, Long> {
}
