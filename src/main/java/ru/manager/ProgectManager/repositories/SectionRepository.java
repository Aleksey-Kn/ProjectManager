package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.documents.Section;

public interface SectionRepository extends CrudRepository<Section, Long> {
}
