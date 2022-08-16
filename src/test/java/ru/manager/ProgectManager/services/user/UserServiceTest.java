package ru.manager.ProgectManager.services.user;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.manager.ProgectManager.DTO.UserDetailsDTO;
import ru.manager.ProgectManager.DTO.request.user.LocaleRequest;
import ru.manager.ProgectManager.DTO.request.user.RegisterUserDTO;
import ru.manager.ProgectManager.DTO.response.project.ProjectResponse;
import ru.manager.ProgectManager.DTO.response.user.MyselfUserDataResponse;
import ru.manager.ProgectManager.DTO.response.user.PublicAllDataResponse;
import ru.manager.ProgectManager.DTO.response.user.VisitMarkResponse;
import ru.manager.ProgectManager.base.ProjectManagerTestBase;
import ru.manager.ProgectManager.entitys.user.ApproveActionToken;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.ActionType;
import ru.manager.ProgectManager.enums.Locale;
import ru.manager.ProgectManager.exception.user.IncorrectLoginOrPasswordException;
import ru.manager.ProgectManager.repositories.ApproveActionTokenRepository;
import ru.manager.ProgectManager.services.project.ProjectService;
import ru.manager.ProgectManager.support.TestDataBuilder;

import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest extends ProjectManagerTestBase {
    @Autowired
    ApproveActionTokenRepository approveActionTokenRepository;

    @Autowired
    ProjectService projectService;

    @Test
    void saveUser() {
        final RegisterUserDTO registerUserDTO = TestDataBuilder.buildMasterUserDto();
        assertThat(userService.saveUser(registerUserDTO)).isPresent()
                .isEqualTo(Optional.of(registerUserDTO.getLogin()));
    }

    @Test
    void updateLastVisitAndZone() {
        final String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        userService.updateLastVisitAndZone(login, 1);
        assertThat(userService.findZoneIdForThisUser(login)).isEqualTo(1);
    }

    @Test
    void enabledUser() {
        final String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        assertInTransaction(() -> {
            String token = StreamSupport.stream(approveActionTokenRepository.findAll().spliterator(), true)
                    .filter(t -> t.getUser().getUsername().equals(login))
                    .map(ApproveActionToken::getToken)
                    .findAny().orElseThrow();
            assertThat(userService.enabledUser(token)).isPresent().isEqualTo(Optional.of("masterUser"));
        });
    }

    @Test
    void attemptDropPass() {
        final var registerDto = TestDataBuilder.buildMasterUserDto();
        final String login = userService.saveUser(registerDto).orElseThrow();
        assertThat(userService.attemptDropPass(login, "url")).isTrue();
    }

    @Test
    void resetPass() {
        final String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        assertInTransaction(() -> {
            userService.attemptDropPass(login, "url");
            String token = StreamSupport.stream(approveActionTokenRepository.findAll().spliterator(), true)
                    .filter(t -> t.getUser().getUsername().equals(login))
                    .filter(t -> t.getActionType() == ActionType.RESET_PASS)
                    .map(ApproveActionToken::getToken)
                    .findAny().orElseThrow();
            assertThat(userService.resetPass(token, "123456")).isTrue();
        });
    }

    @Test
    void findUserDetailsByUsername() {
        final String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        assertThat(userService.findUserDetailsByUsername(login))
                .satisfies(v -> {
                    assertThat(v).extracting(UserDetailsDTO::getUsername).isEqualTo(login);
                    assertThat(v).extracting(UserDetailsDTO::isAccountNonLocked).isEqualTo(true);
                    assertThat(v).extracting(UserDetailsDTO::isEnabled).isEqualTo(false);
                    assertThat(v).extracting(UserDetailsDTO::getZoneId).isEqualTo(7);
                });
    }

    @Test
    void findMyselfUserDataResponseByUsername() {
        final RegisterUserDTO registerUserDTO = TestDataBuilder.buildMasterUserDto();
        final String login = userService.saveUser(registerUserDTO).orElseThrow();
        assertThat(userService.findMyselfUserDataResponseByUsername(login))
                .satisfies(v -> {
                    assertThat(v).extracting(MyselfUserDataResponse::getLogin).isEqualTo(login);
                    assertThat(v).extracting(MyselfUserDataResponse::getNickname).isEqualTo(registerUserDTO.getNickname());
                    assertThat(v.getId() > 0).isTrue();
                });
    }

    @Test
    @SneakyThrows
    void findById() {
        final RegisterUserDTO registerUserDTO = TestDataBuilder.buildMasterUserDto();
        final String login = userService.saveUser(registerUserDTO).orElseThrow();
        final long id = userRepository.findByUsername(login).getUserId();
        assertThat(userService.findById(id, login)).extracting(PublicAllDataResponse::getNickname)
                .isEqualTo(registerUserDTO.getNickname());
    }

    @Test
    @SneakyThrows
    void login() {
        final var registerUserDTO = TestDataBuilder.buildMasterUserDto();
        userService.saveUser(registerUserDTO);
        jdbcTemplate.update("update project_manager.user set enabled = 1");
        assertThat(userService.login(TestDataBuilder.buildAuthDto())).isEqualTo(registerUserDTO.getLogin());
    }

    @Test
    void renameUser() {
        final String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        userService.renameUser(login, "Bulba");
        assertThat(userService.findMyselfUserDataResponseByUsername(login))
                .extracting(MyselfUserDataResponse::getNickname)
                .isEqualTo("Bulba");
    }

    @Test
    @SneakyThrows
    void updatePass() {
        final String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        userService.updatePass("1234", "0000", login);
        assertThatThrownBy(() -> userService.login(TestDataBuilder.buildAuthDto()))
                .isInstanceOf(IncorrectLoginOrPasswordException.class);
    }

    @Test
    void updateLocale() {
        final String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        userService.updateLocale(new LocaleRequest(Locale.ru), login);
        assertInTransaction(() -> assertThat(userRepository.findByUsername(login)).extracting(User::getLocale)
                .isEqualTo(Locale.ru));
    }

    @Test
    void allProjectOfThisUser() {
        final String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        final var firstProject = TestDataBuilder.prepareProjectDataRequest()
                .status("firstStatus").name("firstName").build();
        final var secondProject = TestDataBuilder.prepareProjectDataRequest()
                        .status("secondStatus").name("secondName").build();
        projectService.addProject(firstProject, login);
        projectService.addProject(secondProject, login);
        assertThat(userService.allProjectOfThisUser(login)).satisfies(c -> {
            assertThat(c).extracting(ProjectResponse::getName).contains("firstName", "secondName");
            assertThat(c).extracting(ProjectResponse::getStatus).contains("firstStatus", "secondStatus");
        });
    }

    @Test
    void projectsByNameOfThisUser() {
        final String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        final var firstProject = TestDataBuilder.prepareProjectDataRequest()
                .status("firstStatus").name("firstName").build();
        final var secondProject = TestDataBuilder.prepareProjectDataRequest()
                .status("secondStatus").name("secondName").build();
        projectService.addProject(firstProject, login);
        projectService.addProject(secondProject, login);
        assertThat(userService.projectsByNameOfThisUser("second", login))
                .extracting(ProjectResponse::getStatus)
                .containsOnly("secondStatus");
    }

    @Test
    void availableResourceByName() {
        final String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        final var firstProject = TestDataBuilder.prepareProjectDataRequest()
                .status("firstStatus").name("firstName").build();
        final var secondProject = TestDataBuilder.prepareProjectDataRequest()
                .status("secondStatus").name("secondName").build();
        projectService.addProject(firstProject, login);
        projectService.addProject(secondProject, login);
        assertThat(userService.projectsByNameOfThisUser("first", login))
                .extracting(ProjectResponse::getName)
                .containsOnly("firstName");
    }

    @Test
    @SneakyThrows
    void lastVisits() {
        final String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        final var project = TestDataBuilder.buildProjectDataRequest();
        final var projectId = projectService.addProject(project, login).getId();
        projectService.findProject(projectId, login);
        assertThat(userService.lastVisits(login)).hasSize(1).satisfies(v -> {
            assertThat(v).extracting(VisitMarkResponse::getType).contains("project");
            assertThat(v).extracting(VisitMarkResponse::getName).contains(project.getName());
        });
    }

    @Test
    void findZoneIdForThisUser() {
        final String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        assertThat(userService.findZoneIdForThisUser(login)).isEqualTo(7);
    }
}