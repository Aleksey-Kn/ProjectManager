package ru.manager.ProgectManager.components;

import org.springframework.stereotype.Component;
import ru.manager.ProgectManager.enums.Locale;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class LocalisedMessages {
    public String buildSubjectForMailApprove(Locale locale) {
        return switch(locale) {
            case en -> "Mail confirmation";
            case ru -> "Подтверждение адреса электронной почты";
        };
    }

    public String buildSubjectForResetPassword(Locale locale) {
        return switch(locale) {
            case en -> "Reset password";
            case ru -> "Сброс пароля";
        };
    }

    public String buildSubjectForInvitationToProject(Locale locale, String projectName) {
        return switch (locale) {
            case en -> "Invitation to the project '" + projectName + "'";
            case ru -> "Приглашение в проект '" + projectName + "'";
        };
    }

    public String buildSubjectAboutAuthorisation(Locale locale) {
        return switch (locale) {
            case ru -> "Зафиксирован вход в ваш аккаунт";
            case en -> "Signed in to your account";
        };
    }

    public String buildTextForMailApprove(Locale locale, String url, String token) {
        return switch (locale) {
            case en -> "For approval your account follow this link: ";
            case ru -> "Для подтверждения вашего аккаунта перейдите по ссылке: ";
        } + url + "?token=" + token;
    }

    public String buildTextForResetPass(Locale locale, String url, String token) {
        return switch (locale) {
            case en -> "An attempt was made to reset the password for your account. " +
                    "If you would like to take this action, please follow this link: ";
            case ru -> "Была осуществлена попытка сброса пароля для вашего аккаунта. " +
                    "Если вы желаете совершить это действие, перейдите по данной ссылке: ";
        } + url + "?token=" + token;
    }

    public String buildTextForInvitationToProject(Locale locale, String projectName, String url, String token) {
        return switch (locale) {
            case en -> "In order to join the project '" + projectName + "' , you need to follow the link: ";
            case ru -> "Для того, чтобы присоединиться к проекту '" + projectName + "', перейдите по данной ссылке: ";
        } + url + "?token=" + token;
    }

    public String buildTextAboutAuthorisation(Locale locale, String ip, String browser, String country, String city){
        String date = LocalDate.now().toString();
        String time = LocalTime.now().toString();
        time = time.substring(0, time.indexOf('.'));
        return switch (locale) {
            case ru -> String.format("Зарегистрирован вход в ваш аккаунт из браузера %s через ip-адрес %s, " +
                    "находящийся в %s, %s. Авторизация произошла %s в %s.", browser, ip, city, country, date, time);
            case en -> String.format("Logged in to your account from a browser %s using ip-address %s located in %s, %s. " +
                    "Authorization reproduced by %s in %s.", browser, ip, city, country, date, time);
        };
    }
}
