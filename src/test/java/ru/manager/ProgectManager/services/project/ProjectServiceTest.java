package ru.manager.ProgectManager.services.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.manager.ProgectManager.DTO.request.ProjectDataRequest;
import ru.manager.ProgectManager.DTO.response.project.ProjectResponseWithFlag;
import ru.manager.ProgectManager.DTO.response.user.UserDataWithProjectRoleResponse;
import ru.manager.ProgectManager.base.ProjectManagerTestBase;
import ru.manager.ProgectManager.support.TestDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectServiceTest extends ProjectManagerTestBase {
    @Autowired
    ProjectService projectService;

    @Test
    void findProject() {
        String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        long id = projectService.addProject(TestDataBuilder.buildProjectDataRequest(), login).getId();

        final var response = projectService.findProject(id, login);
        final var expected = TestDataBuilder.buildProjectResponseWithFlag(id);
        assertThat(response)
                .isEqualTo(expected);
    }

    @Test
    void addProject() {
        String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        assertThat(projectService.addProject(TestDataBuilder.buildProjectDataRequest(), login))
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(TestDataBuilder.prepareProjectResponseWithFlag(0));
    }

    @Test
    void setData() {
        String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        long id = projectService.addProject(TestDataBuilder.buildProjectDataRequest(), login).getId();
        ProjectDataRequest updatedData = TestDataBuilder.buildProjectDataRequest();
        ProjectResponseWithFlag targetProject = TestDataBuilder.buildProjectResponseWithFlag(id);

        projectService.setData(id, updatedData, login);
        assertThat(projectService.findProject(id, login)).isEqualTo(targetProject);
    }

    @Test
    void deleteProject() {
        String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        long id = projectService.addProject(TestDataBuilder.buildProjectDataRequest(), login).getId();

        projectService.deleteProject(id, login);
        assertThat(jdbcTemplate.queryForObject("select exists(select * from project where id=?)",
                Boolean.class, id)).isFalse();
    }

    @Test
    void findAllMembers() {
        String first = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        long id = projectService.addProject(TestDataBuilder.buildProjectDataRequest(), first).getId();

        assertThat(projectService.findAllMembers(id, first))
                .extracting(UserDataWithProjectRoleResponse::getNickname)
                .isEqualTo(first);
    }

    @Test
    void findMembersByNicknameOrEmail() {
        String first = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        long id = projectService.addProject(TestDataBuilder.buildProjectDataRequest(), first).getId();

        assertThat(projectService.findMembersByNicknameOrEmail(id, "User", first))
                .extracting(UserDataWithProjectRoleResponse::getNickname)
                .isEqualTo(first);
    }

    @Test
    void canCreateOrDeleteResources() {
        String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        long id = projectService.addProject(TestDataBuilder.buildProjectDataRequest(), login).getId();

        assertThat(projectService.canCreateOrDeleteResources(id, login))
                .isTrue();
    }
}