package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.components.LocalisedMessages;
import ru.manager.ProgectManager.entitys.ApproveActionToken;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.enums.ActionType;
import ru.manager.ProgectManager.enums.Locale;
import ru.manager.ProgectManager.repositories.ApproveActionTokenRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;
    private final ApproveActionTokenRepository approveActionTokenRepository;
    private final LocalisedMessages localisedMessages;

    public void sendEmailApprove(User user, String url, Locale locale) {
        String token = UUID.randomUUID().toString();
        send(user.getEmail(), localisedMessages.buildSubjectForMailApprove(locale),
                localisedMessages.buildTextForMailApprove(locale, url, token));

        ApproveActionToken approveActionToken = new ApproveActionToken();
        approveActionToken.setToken(token);
        approveActionToken.setUser(user);
        approveActionToken.setActionType(ActionType.APPROVE_ENABLE);
        approveActionTokenRepository.save(approveActionToken);
    }

    public void sendResetPass(User user, String url, Locale locale) {
        String token = UUID.randomUUID().toString();
        send(user.getEmail(), localisedMessages.buildSubjectForResetPassword(locale),
                localisedMessages.buildTextForResetPass(locale, url, token));

        ApproveActionToken approveActionToken = new ApproveActionToken();
        approveActionToken.setToken(token);
        approveActionToken.setUser(user);
        approveActionToken.setActionType(ActionType.RESET_PASS);
        approveActionTokenRepository.save(approveActionToken);
    }

    public void sendInvitationToProject(String email, String projectName, String url, String token, Locale locale) {
        send(email, localisedMessages.buildSubjectForInvitationToProject(locale, projectName),
                localisedMessages.buildTextForInvitationToProject(locale, projectName, url, token));
    }

    private void send(String address, String subject, String text) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(address);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);
        javaMailSender.send(mailMessage);
    }
}
