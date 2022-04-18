package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.documents.Page;

public interface PageRepository extends CrudRepository<Page, Long> {
}
