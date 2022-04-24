package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.entitys.ApproveEnabledUser;
import ru.manager.ProgectManager.entitys.User;
import ru.manager.ProgectManager.repositories.ApproveEnabledUserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;
    private final ApproveEnabledUserRepository approveEnabledUserRepository;

    public void sendEmailApprove(User user, String url) {
        String token = UUID.randomUUID().toString();
        send(user.getEmail(), "Mail confirmation",
                "For approvement your account follow this link: " + url + "?token=" + token);

        ApproveEnabledUser approveEnabledUser = new ApproveEnabledUser();
        approveEnabledUser.setToken(token);
        approveEnabledUser.setUser(user);
        approveEnabledUserRepository.save(approveEnabledUser);
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
