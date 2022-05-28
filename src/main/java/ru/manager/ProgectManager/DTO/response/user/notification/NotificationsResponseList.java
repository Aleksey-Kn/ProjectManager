package ru.manager.ProgectManager.DTO.response.user.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.user.Notification;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Ответ об уведомлениях пользователя")
public class NotificationsResponseList {
    @Schema(description = "Непрочитанные уведомления")
    private final List<NotificationResponse> unreadNotifications;
    @Schema(description = "Прочитанные уведомления")
    private final List<NotificationResponse> readNotifications;

    public NotificationsResponseList(Set<Notification> notifications, int zoneId) {
        unreadNotifications = notifications.stream()
                .filter(Notification::isNewNotification)
                .sorted(Comparator.comparing(Notification::getCreateDatetime).reversed())
                .map(notification -> new NotificationResponse(notification, zoneId))
                .collect(Collectors.toList());

        readNotifications = notifications.stream()
                .filter(notification -> !notification.isNewNotification())
                .sorted(Comparator.comparing(Notification::getCreateDatetime).reversed())
                .map(notification -> new NotificationResponse(notification, zoneId))
                .collect(Collectors.toList());
    }
}
