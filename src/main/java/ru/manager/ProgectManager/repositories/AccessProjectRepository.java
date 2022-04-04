package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.accessProject.AccessProject;

public interface AccessProjectRepository extends CrudRepository<AccessProject, String> {
}
