package ru.manager.ProgectManager.services.user;

import com.icegreen.greenmail.util.GreenMailUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.manager.ProgectManager.DTO.UserDetailsDTO;
import ru.manager.ProgectManager.DTO.request.user.RegisterUserDTO;
import ru.manager.ProgectManager.DTO.response.user.MyselfUserDataResponse;
import ru.manager.ProgectManager.DTO.response.user.PublicAllDataResponse;
import ru.manager.ProgectManager.base.ProjectManagerTestBase;
import ru.manager.ProgectManager.entitys.user.ApproveActionToken;
import ru.manager.ProgectManager.enums.ActionType;
import ru.manager.ProgectManager.enums.Locale;
import ru.manager.ProgectManager.exception.user.IncorrectLoginOrPasswordException;
import ru.manager.ProgectManager.repositories.ApproveActionTokenRepository;
import ru.manager.ProgectManager.support.TestDataBuilder;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest extends ProjectManagerTestBase {

    @Autowired
    ApproveActionTokenRepository approveActionTokenRepository;

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
    void attemptDropPass() throws MessagingException {
        final var registerDto = TestDataBuilder.buildMasterUserDto();
        final String login = userService.saveUser(registerDto).orElseThrow();
        assertThat(userService.attemptDropPass(login, "url")).isTrue();
//        MimeMessage receivedMessage = GREEN_MAIL.getReceivedMessages()[0];
//        assertThat(receivedMessage.getAllRecipients()).hasSize(1);
        assertThat(GREEN_MAIL.getReceivedMessagesForDomain(registerDto.getEmail()))
                .extracting(MimeMessage::getSubject)
                .contains(localisedMessages.buildSubjectForResetPassword(Locale.en));
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
    void findById() {
        final RegisterUserDTO registerUserDTO = TestDataBuilder.buildMasterUserDto();
        final String login = userService.saveUser(registerUserDTO).orElseThrow();
        final long id = userRepository.findByUsername(login).getUserId();
        assertThat(userService.findById(id, login)).extracting(PublicAllDataResponse::getNickname)
                .isEqualTo(registerUserDTO.getNickname());
    }

    @Test
    void login() {
        final var registerUserDTO = TestDataBuilder.buildMasterUserDto();
        userService.saveUser(registerUserDTO);
        assertThat(userService.login(TestDataBuilder.buildAuthDto())).isEqualTo(registerUserDTO.getLogin());
        assertThat(GREEN_MAIL.getReceivedMessagesForDomain(registerUserDTO.getEmail()))
                .extracting(MimeMessage::getSubject)
                .contains(localisedMessages.buildSubjectAboutAuthorisation(Locale.en));
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
    void updatePass() {
        final String login = userService.saveUser(TestDataBuilder.buildMasterUserDto()).orElseThrow();
        userService.updatePass("1234", "0000", login);
        assertThatThrownBy(() -> userService.login(TestDataBuilder.buildAuthDto()))
                .isInstanceOf(IncorrectLoginOrPasswordException.class);
    }

    @Test
    void updateLocale() {
    }

    @Test
    void allProjectOfThisUser() {
    }

    @Test
    void projectsByNameOfThisUser() {
    }

    @Test
    void availableResourceByName() {
    }

    @Test
    void lastVisits() {
    }

    @Test
    void findZoneIdForThisUser() {
    }

    @Test
    void setMailService() {
    }

    @Test
    void setApproveEnabledUserRepository() {
    }

    @Test
    void setUsedAddressRepository() {
    }

    @Test
    void setNotificationService() {
    }

    @Test
    void setCompressor() {
    }

    @Test
    void setRefreshTokenRepository() {
    }

    @Test
    void setLocalisedMessages() {
    }

    @Test
    void setMailInfoRepository() {
    }
}