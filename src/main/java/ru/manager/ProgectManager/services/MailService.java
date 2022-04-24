package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.entitys.ApproveActionToken;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.enums.ActionType;
import ru.manager.ProgectManager.repositories.ApproveActionTokenRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;
    private final ApproveActionTokenRepository approveActionTokenRepository;

    public void sendEmailApprove(User user, String url) {
        String token = UUID.randomUUID().toString();
        send(user.getEmail(), "Mail confirmation",
                "For approvement your account follow this link: " + url + "?token=" + token);

        ApproveActionToken approveActionToken = new ApproveActionToken();
        approveActionToken.setToken(token);
        approveActionToken.setUser(user);
        approveActionToken.setActionType(ActionType.APPROVE_ENABLE);
        approveActionTokenRepository.save(approveActionToken);
    }

    public void sendResetPass(User user, String url) {
        String token = UUID.randomUUID().toString();
        send(user.getEmail(), "Reset password", "For reset password follow this link: \n" + url + "?token=" +
                token + "\nIf you have not tried to reset your password, please ignore this message.");

        ApproveActionToken approveActionToken = new ApproveActionToken();
        approveActionToken.setToken(token);
        approveActionToken.setUser(user);
        approveActionToken.setActionType(ActionType.RESET_PASS);
        approveActionTokenRepository.save(approveActionToken);
    }

    public void sendInvitationToProject(String email, String projectName, String url, String token) {
        send(email, "Invitation to the project '" + projectName + "'",
                "In order to join the project, you need to follow the link: " + url + "?token=" + token);
    }

    private void send(String address, String subject, String text) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(address);
        mailMessage.setSubject(subject);
        mailMessage.setText(text);
        javaMailSender.send(mailMessage);
    }
}
