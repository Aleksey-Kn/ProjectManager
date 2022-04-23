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
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Mail confirmation");
        mailMessage.setText("For approvement your account follow this link: " + url + "?token=" + token);
        javaMailSender.send(mailMessage);

        ApproveEnabledUser approveEnabledUser = new ApproveEnabledUser();
        approveEnabledUser.setToken(token);
        approveEnabledUser.setUser(user);
        approveEnabledUserRepository.save(approveEnabledUser);
    }
}
