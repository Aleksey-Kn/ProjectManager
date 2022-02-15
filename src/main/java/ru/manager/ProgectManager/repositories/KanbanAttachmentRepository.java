package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.KanbanAttachment;

public interface KanbanAttachmentRepository extends CrudRepository<KanbanAttachment, Long> {
}
