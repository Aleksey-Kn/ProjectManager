package ru.manager.ProgectManager.services.kanban;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.manager.ProgectManager.base.ProjectManagerTestBase;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.kanban.NoSuchKanbanException;
import ru.manager.ProgectManager.exception.project.NoSuchProjectException;
import ru.manager.ProgectManager.services.project.ProjectService;
import ru.manager.ProgectManager.support.TestDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KanbanServiceTest extends ProjectManagerTestBase {
    @Autowired
    KanbanService kanbanService;

    @Autowired
    ProjectService projectService;

    private long projectId;
    private String login;

    @BeforeEach
    void createProject() {
        login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        projectId = projectService.addProject(TestDataBuilder.buildProjectDataRequest(), login).getId();
    }

    @AfterEach
    void clear() throws ForbiddenException, NoSuchProjectException {
        projectService.deleteProject(projectId, login);
    }

    @SneakyThrows
    @Test
    void createKanban() {
        final long id = kanbanService.createKanban(projectId, "Board", login).getId();
        assertThat(kanbanService.findKanban(id, login, 0, 10)).satisfies(v -> {
            assertThat(v.getName()).isEqualTo("Board");
            assertThat(v.getKanbanColumns()).isEmpty();
            assertThat(v.isCanEdit()).isTrue();
        });
    }

    @Test
    @SneakyThrows
    void removeKanban() {
        final long id = kanbanService.createKanban(projectId, "Board", login).getId();
        kanbanService.removeKanban(id, login);
        assertThatThrownBy(() -> kanbanService.findKanban(id, login, 0, 10))
                .isInstanceOf(NoSuchKanbanException.class);
    }

    @Test
    @SneakyThrows
    void rename() {
        final long id = kanbanService.createKanban(projectId, "Board", login).getId();
        kanbanService.rename(id, "Kanban", login);
        assertThat(kanbanService.findKanban(id, login, 0, 10).getName()).isEqualTo("Kanban");
    }

    @Test
    void findKanban() {
    }

    @Test
    void findAllKanban() {
    }

    @Test
    void findKanbansByName() {
    }

    @Test
    void addTag() {
    }

    @Test
    void removeTag() {
    }

    @Test
    void editTag() {
    }

    @Test
    void findAllAvailableTags() {
    }

    @Test
    void members() {
    }
}