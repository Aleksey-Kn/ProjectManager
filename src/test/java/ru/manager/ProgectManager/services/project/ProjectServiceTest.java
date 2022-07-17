package ru.manager.ProgectManager.services.project;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.manager.ProgectManager.DTO.request.ProjectDataRequest;
import ru.manager.ProgectManager.DTO.response.user.UserDataListResponse;
import ru.manager.ProgectManager.DTO.response.user.UserDataWithProjectRoleResponse;
import ru.manager.ProgectManager.base.ProjectManagerTestBase;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.support.TestDataBuilder;

import java.util.List;
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
        String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow().getUsername();
        assertThat(projectService.addProject(TestDataBuilder.buildProjectDto(), login))
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(TestDataBuilder.buildProject(0));
    }

    @Test
    void setData() {
        String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow().getUsername();
        long id = projectService.addProject(TestDataBuilder.buildProjectDto(), login).getId();
        ProjectDataRequest updatedData = TestDataBuilder.prepareProjectRequest().status("Done").name("New name").build();
        Project targetProject = TestDataBuilder.buildProject(id);
        targetProject.setStatus("Done");
        targetProject.setName("New name");

        assertThat(projectService.setData(id, updatedData, login)).isTrue();
        assertThat(projectService.findProject(id, login)).isPresent().isEqualTo(Optional.of(targetProject));
    }

    @Test
    void deleteProject() {
        String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow().getUsername();
        long id = projectService.addProject(TestDataBuilder.buildProjectDto(), login).getId();

        assertThat(projectService.deleteProject(id, login)).isTrue();
        assertThat(jdbcTemplate.queryForObject("select exists(select * from project where id=?)",
                Boolean.class, id)).isFalse();
    }

    @Test
    void findAllMembers() {
        User first = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        UserDataWithProjectRoleResponse response =
                new UserDataWithProjectRoleResponse(first, TypeRoleProject.ADMIN.name(), 7);
        long id = projectService.addProject(TestDataBuilder.buildProjectDto(), first.getUsername()).getId();

        assertThat(projectService.findAllMembers(id, first.getUsername()).orElseThrow())
                .isEqualTo(new UserDataListResponse(List.of(response)));
    }

    @Test
    void findMembersByNicknameOrEmail() {
        User first = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        UserDataWithProjectRoleResponse response =
                new UserDataWithProjectRoleResponse(first, TypeRoleProject.ADMIN.name(), 7);
        long id = projectService.addProject(TestDataBuilder.buildProjectDto(), first.getUsername()).getId();

        assertThat(projectService.findMembersByNicknameOrEmail(id, "User", first.getUsername()).orElseThrow())
                .isEqualTo(new UserDataListResponse(List.of(response)));
    }

    @Test
    void canCreateOrDeleteResources() {
        String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow().getUsername();
        long id = projectService.addProject(TestDataBuilder.buildProjectDto(), login).getId();

        assertThat(projectService.canCreateOrDeleteResources(projectService.findProject(id, login).orElseThrow(), login))
                .isTrue();
    }
}