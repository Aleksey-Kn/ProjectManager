package ru.manager.ProgectManager.DTO.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import ru.manager.ProgectManager.entitys.user.Notification;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Schema(description = "Ответ об уведомлениях пользователя")
public class NotificationsResponse {
    @Schema(description = "Непрочитанные уведомления")
    private final List<String> unreadNotifications;
    @Schema(description = "Прочитанные уведомления")
    private final List<String> readNotifications;

    public NotificationsResponse(Set<Notification> notifications) {
        unreadNotifications = notifications.stream()
                .filter(Notification::isNewNotification)
                .sorted(Comparator.comparing(Notification::getCreateDatetime).reversed())
                .map(Notification::getText)
                .collect(Collectors.toList());

        readNotifications = notifications.stream()
                .filter(notification -> !notification.isNewNotification())
                .sorted(Comparator.comparing(Notification::getCreateDatetime).reversed())
                .map(Notification::getText)
                .collect(Collectors.toList());

    }
}
