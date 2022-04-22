package ru.manager.ProgectManager.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${server.host}")
    private String host;

    @Value("${server.port}")
    private String port;

    public void sendEmailApprove(User user) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Mail confirmation");

        ApproveEnabledUser approveEnabledUser = new ApproveEnabledUser();
        String token = UUID.randomUUID().toString();
        approveEnabledUser.setToken(token);
        approveEnabledUser.setUser(user);
        approveEnabledUserRepository.save(approveEnabledUser);

        mailMessage.setText("For approvement your account follow this link: " +
                String.format("https://%s:%s/approve?token=%s", host, port, token));
        javaMailSender.send(mailMessage);
    }
}
