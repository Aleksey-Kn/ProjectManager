package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.documents.Page;

public interface SectionRepository extends CrudRepository<Page, Long> {
}
