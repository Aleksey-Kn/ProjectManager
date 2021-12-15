package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.UserWithRoleConnector;

public interface UserWithRoleConnectorRepository extends CrudRepository<UserWithRoleConnector, Integer> {
}
