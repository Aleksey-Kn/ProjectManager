package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.entitys.user.Note;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.repositories.NoteRepository;
import ru.manager.ProgectManager.repositories.UserRepository;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;

    public boolean setNote(String noteText, long targetUserId, String ownerLogin) {
        User ownerNote = userRepository.findByUsername(ownerLogin);
        if (!userRepository.existsById(targetUserId))
            throw new NoSuchElementException();
        if(ownerNote.getUserId() != targetUserId) {
            ownerNote.getNotes().parallelStream()
                    .filter(note -> note.getUserId() == targetUserId)
                    .findAny()
                    .ifPresentOrElse(note -> {
                        note.setText(noteText);
                        noteRepository.save(note);
                    }, () -> {
                        Note note = new Note();
                        note.setUserId(targetUserId);
                        note.setText(noteText);
                        ownerNote.getNotes().add(noteRepository.save(note));
                        userRepository.save(ownerNote);
                    });
            return true;
        } else {
            return false;
        }
    }

    public boolean deleteNote(long targetUserId, String ownerLogin) {
        User owner = userRepository.findByUsername(ownerLogin);
        Optional<Note> note = noteRepository.findById(targetUserId);
        if(note.isPresent()) {
            owner.getNotes().remove(note.get());
            userRepository.save(owner);
            noteRepository.delete(note.get());
            return true;
        } else {
            return false;
        }
    }

    public Optional<Note> findNote(long targetUserId, String ownerLogin) {
        return userRepository.findByUsername(ownerLogin).getNotes().parallelStream()
                .filter(note -> note.getUserId() == targetUserId)
                .findAny();
    }
}
