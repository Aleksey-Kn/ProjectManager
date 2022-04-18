package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.documents.Page;

import java.util.Set;

public interface PageRepository extends CrudRepository<Page, Long> {
    Set<Page> findPageByProject(Project project);
}
