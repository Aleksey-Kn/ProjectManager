package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.CustomProjectRole;

import java.util.Optional;

public interface CustomProjectRoleRepository extends CrudRepository<CustomProjectRole, Long> {
    Optional<CustomProjectRole> findByName(String name);
}
