package ru.manager.ProgectManager.services.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.user.AuthDto;
import ru.manager.ProgectManager.components.LocalisedMessages;
import ru.manager.ProgectManager.entitys.user.Notification;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.repositories.NotificationRepository;
import ru.manager.ProgectManager.repositories.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final LocalisedMessages localisedMessages;

    public void addNotificationAboutAuthorisation(AuthDto authDto, User forUser) {
        Notification notification = new Notification();
        notification.setNewNotification(true);
        notification.setCreateDatetime(LocalDateTime.now()
                .toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())));
        notification.setText(localisedMessages.buildTextAboutAuthorisation(forUser.getLocale(), authDto.getIp(),
                authDto.getBrowser(), authDto.getCountry(), authDto.getCity(), authDto.getZoneId()));
        forUser.getNotifications().add(notificationRepository.save(notification));
        userRepository.save(forUser);
    }

    public void addNotificationAboutDeleteFromProject(String projectName, User forUser) {
        Notification notification = new Notification();
        notification.setNewNotification(true);
        notification.setCreateDatetime(LocalDateTime.now()
                .toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())));
        notification.setText(localisedMessages.buildTextAboutDeleteFromProject(forUser.getLocale(), projectName));
        forUser.getNotifications().add(notificationRepository.save(notification));
        userRepository.save(forUser);
    }

    public void readNotification(String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        user.getNotifications().forEach(notification -> {
            notification.setNewNotification(false);
            notificationRepository.save(notification);
        });
    }

    public boolean hasNewNotification(String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        return user.getNotifications().parallelStream().anyMatch(Notification::isNewNotification);
    }
}
