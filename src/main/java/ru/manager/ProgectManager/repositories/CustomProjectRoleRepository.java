package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.accessProject.CustomProjectRole;

public interface CustomProjectRoleRepository extends CrudRepository<CustomProjectRole, Long> {
}
