package ru.manager.ProgectManager.services.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.manager.ProgectManager.entitys.user.Note;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.exception.user.NoSuchUserException;
import ru.manager.ProgectManager.repositories.NoteRepository;
import ru.manager.ProgectManager.repositories.UserRepository;

import java.util.Optional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class NoteService {
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;

    @Transactional
    public boolean setNote(String noteText, long targetUserId, String ownerLogin) throws NoSuchUserException {
        User ownerNote = userRepository.findByUsername(ownerLogin);
        if (!userRepository.existsById(targetUserId))
            throw new NoSuchUserException();
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

    @Transactional
    public boolean deleteNote(long targetUserId, String ownerLogin) {
        User owner = userRepository.findByUsername(ownerLogin);
        Optional<Note> note = noteRepository.findByUserId(targetUserId);
        if(note.isPresent()) {
            owner.getNotes().remove(note.get());
            userRepository.save(owner);
            noteRepository.delete(note.get());
            return true;
        } else {
            return false;
        }
    }

    public String findNote(long targetUserId, String ownerLogin) {
        return userRepository.findByUsername(ownerLogin).getNotes().parallelStream()
                .filter(note -> note.getUserId() == targetUserId)
                .findAny()
                .map(Note::getText)
                .orElse(null);
    }
}
