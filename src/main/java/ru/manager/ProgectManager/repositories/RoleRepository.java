package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.user.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Role findByName(String name);
}
