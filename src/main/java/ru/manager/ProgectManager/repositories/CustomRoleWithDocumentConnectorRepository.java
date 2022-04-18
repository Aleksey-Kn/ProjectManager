package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.accessProject.CustomRoleWithDocumentConnector;

public interface CustomRoleWithDocumentConnectorRepository extends CrudRepository<CustomRoleWithDocumentConnector, Long> {
}
