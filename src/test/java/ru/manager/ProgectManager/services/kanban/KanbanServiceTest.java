package ru.manager.ProgectManager.services.kanban;

import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanMainDataResponse;
import ru.manager.ProgectManager.DTO.response.kanban.TagResponse;
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
        assertThat(kanbanService.createKanban(projectId, "Board", login).getId()).isPositive();
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
    @SneakyThrows
    void findKanban() {
        final long id = kanbanService.createKanban(projectId, "Board", login).getId();
        assertThat(kanbanService.findKanban(id, login, 0, 10))
                .isEqualTo(TestDataBuilder.buildKanbanContentResponse(id));
    }

    @Test
    @SneakyThrows
    void findAllKanban() {
        kanbanService.createKanban(projectId, "Board", login);
        kanbanService.createKanban(projectId, "Kanban", login);
        assertThat(kanbanService.findAllKanban(projectId, login)).extracting(KanbanMainDataResponse::getName)
                .containsOnly("Board", "Kanban");
    }

    @Test
    @SneakyThrows
    void findKanbansByName() {
        final long id = kanbanService.createKanban(projectId, "Board", login).getId();
        kanbanService.createKanban(projectId, "Kanban", login);
        assertThat(kanbanService.findKanbansByName(projectId, "bo", login))
                .extracting(KanbanMainDataResponse::getId)
                .containsOnly(id);
    }

    @Test
    @SneakyThrows
    void addTag() {
        final long id = kanbanService.createKanban(projectId, "Board", login).getId();
        kanbanService.addTag(id, TestDataBuilder.buildTagRequest(), login);
        assertThat(kanbanService.findAllAvailableTags(id, login)).extracting(TagResponse::getText)
                .containsOnly("Text");
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