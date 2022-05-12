package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.entitys.user.Note;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.repositories.NoteRepository;
import ru.manager.ProgectManager.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;

    public void setNote(String noteText, long targetUserId, String userLogin) {
        User ownerNote = userRepository.findByUsername(userLogin);
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
    }
}
