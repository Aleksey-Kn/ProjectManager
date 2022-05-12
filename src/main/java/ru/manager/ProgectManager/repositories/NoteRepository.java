package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.user.Note;

public interface NoteRepository extends CrudRepository<Note, Long> {
}
