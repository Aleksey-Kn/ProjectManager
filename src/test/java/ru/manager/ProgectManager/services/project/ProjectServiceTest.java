package ru.manager.ProgectManager.services.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.manager.ProgectManager.base.ProjectManagerTestBase;
import ru.manager.ProgectManager.support.TestDataBuilder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectServiceTest extends ProjectManagerTestBase {
    @Autowired
    ProjectService projectService;

    @Test
    void findProject() {
        String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow().getUsername();
        long id = projectService.addProject(TestDataBuilder.buildProjectDto(), login).getId();

        assertThat(projectService.findProject(id, login)).isPresent()
                .isEqualTo(Optional.of(TestDataBuilder.buildProject(id)));
    }

    @Test
    void addProject() {
    }

    @Test
    void setData() {
    }

    @Test
    void deleteProject() {
    }

    @Test
    void findAllMembers() {
    }

    @Test
    void findMembersByNicknameOrEmail() {
    }

    @Test
    void canCreateOrDeleteResources() {
    }
}