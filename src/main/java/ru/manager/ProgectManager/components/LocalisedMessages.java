package ru.manager.ProgectManager.components;

import org.springframework.stereotype.Component;
import ru.manager.ProgectManager.enums.Locale;

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

    public String buildTextForMailApprove(Locale locale, String url, String token) {
        return switch (locale) {
            case en -> "For approvement your account follow this link: ";
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
}
