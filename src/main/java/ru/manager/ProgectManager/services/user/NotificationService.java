package ru.manager.ProgectManager.services.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.manager.ProgectManager.DTO.request.user.AuthDto;
import ru.manager.ProgectManager.components.LocalisedMessages;
import ru.manager.ProgectManager.entitys.user.Notification;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.repositories.NotificationRepository;
import ru.manager.ProgectManager.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final LocalisedMessages localisedMessages;

    public void addNotificationAboutAuthorisation(AuthDto authDto, User user) {
        Notification notification = new Notification();
        notification.setNewNotification(true);
        notification.setText(localisedMessages.buildTextAboutAuthorisation(user.getLocale(), authDto.getIp(),
                authDto.getBrowser(), authDto.getCountry(), authDto.getCity(), authDto.getZoneId()));
        user.getNotifications().add(notificationRepository.save(notification));
        userRepository.save(user);
    }

    public void addNotificationAboutInvitation(String projectName, String url, String token, User user) {
        Notification notification = new Notification();
        notification.setNewNotification(true);
        notification.setText(localisedMessages.buildTextForInvitationToProject(user.getLocale(), projectName, url, token));
        user.getNotifications().add(notificationRepository.save(notification));
        userRepository.save(user);
    }

    public void readNotification(String userLogin) {
        User user = userRepository.findByUsername(userLogin);
        user.getNotifications().forEach(notification -> {
            notification.setNewNotification(false);
            notificationRepository.save(notification);
        });
    }
}
