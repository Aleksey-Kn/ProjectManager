package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.Project;

public interface ProjectRepository extends CrudRepository<Project, Long> {

}
