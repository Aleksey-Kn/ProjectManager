package ru.manager.ProgectManager.services.user;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.manager.ProgectManager.base.ProjectManagerTestBase;
import ru.manager.ProgectManager.support.TestDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class NoteServiceTest extends ProjectManagerTestBase {
    @Autowired
    NoteService noteService;

    private String ownerLogin;
    private long targetId;

    @BeforeEach
    void setUp() {
        ownerLogin = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        targetId = userService.findMyselfUserDataResponseByUsername(userService
                .saveUser(TestDataBuilder.prepareUser().login("carrier").email("carrier@mail.ru").nickname("I'm Carrier")
                        .build()).orElseThrow()).getId();
    }

    @Test
    @SneakyThrows
    void setNote() {
        assertThat(noteService.setNote("Note", targetId, ownerLogin)).isTrue();
        assertInTransaction(() ->
                assertThat(userRepository.findByUsername(ownerLogin).getNotes().iterator().next().getText())
                        .isEqualTo("Note"));
    }

    @Test
    @SneakyThrows
    void deleteNote() {
        noteService.setNote("Note", targetId, ownerLogin);
        noteService.deleteNote(targetId, ownerLogin);
        assertThat(noteService.findNote(targetId, ownerLogin)).isNull();
    }

    @Test
    @SneakyThrows
    void findNote() {
        noteService.setNote("Note", targetId, ownerLogin);
        assertThat(noteService.findNote(targetId, ownerLogin)).isEqualTo("Note");
    }
}