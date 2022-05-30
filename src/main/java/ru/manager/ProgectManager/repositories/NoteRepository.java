package ru.manager.ProgectManager.repositories;

import org.springframework.data.repository.CrudRepository;
import ru.manager.ProgectManager.entitys.user.Note;

import java.util.Optional;

public interface NoteRepository extends CrudRepository<Note, Long> {
    Optional<Note> findByUserId(long userId);
}
