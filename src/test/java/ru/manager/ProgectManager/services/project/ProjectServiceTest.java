package ru.manager.ProgectManager.services.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.manager.ProgectManager.DTO.request.ProjectDataRequest;
import ru.manager.ProgectManager.DTO.response.project.ProjectResponseWithFlag;
import ru.manager.ProgectManager.DTO.response.user.UserDataListResponse;
import ru.manager.ProgectManager.DTO.response.user.UserDataWithProjectRoleResponse;
import ru.manager.ProgectManager.base.ProjectManagerTestBase;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.support.TestDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectServiceTest extends ProjectManagerTestBase {
    @Autowired
    ProjectService projectService;

    @Test
    void findProject() {
        String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow().getUsername();
        long id = projectService.addProject(TestDataBuilder.buildProjectDataRequest(), login).getId();

        final var response = projectService.findProject(id, login);
        final var expected = TestDataBuilder.buildProjectResponseWithFlag(id);
        assertThat(response)
                .isEqualTo(expected);
    }

    @Test
    void addProject() {
        String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow().getUsername();
        assertThat(projectService.addProject(TestDataBuilder.buildProjectDataRequest(), login))
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(TestDataBuilder.prepareProjectResponseWithFlag(0));
    }

    @Test
    void setData() {
        String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow().getUsername();
        long id = projectService.addProject(TestDataBuilder.buildProjectDataRequest(), login).getId();
        ProjectDataRequest updatedData = TestDataBuilder.buildProjectDataRequest();
        ProjectResponseWithFlag targetProject = TestDataBuilder.buildProjectResponseWithFlag(id);

        projectService.setData(id, updatedData, login);
        assertThat(projectService.findProject(id, login)).isEqualTo(targetProject);
    }

    @Test
    void deleteProject() {
        String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow().getUsername();
        long id = projectService.addProject(TestDataBuilder.buildProjectDataRequest(), login).getId();

        projectService.deleteProject(id, login);
        assertThat(jdbcTemplate.queryForObject("select exists(select * from project where id=?)",
                Boolean.class, id)).isFalse();
    }

    @Test
    void findAllMembers() {
        User first = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        UserDataWithProjectRoleResponse response =
                new UserDataWithProjectRoleResponse(first, TypeRoleProject.ADMIN.name(), 7);
        long id = projectService.addProject(TestDataBuilder.buildProjectDataRequest(), first.getUsername()).getId();

        assertThat(projectService.findAllMembers(id, first.getUsername()))
                .isEqualTo(new UserDataListResponse(List.of(response)));
    }

    @Test
    void findMembersByNicknameOrEmail() {
        User first = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        UserDataWithProjectRoleResponse response =
                new UserDataWithProjectRoleResponse(first, TypeRoleProject.ADMIN.name(), 7);
        long id = projectService.addProject(TestDataBuilder.buildProjectDataRequest(), first.getUsername()).getId();

        assertThat(projectService.findMembersByNicknameOrEmail(id, "User", first.getUsername()))
                .isEqualTo(new UserDataListResponse(List.of(response)));
    }

    @Test
    void canCreateOrDeleteResources() {
        String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow().getUsername();
        long id = projectService.addProject(TestDataBuilder.buildProjectDataRequest(), login).getId();

        assertThat(projectService.canCreateOrDeleteResources(id, login))
                .isTrue();
    }
}