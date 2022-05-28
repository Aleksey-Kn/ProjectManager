package ru.manager.ProgectManager.DTO.response.user.notification;

import lombok.Getter;
import ru.manager.ProgectManager.entitys.user.Notification;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
public class NotificationResponse {
    private final String text;
    private final String createDatetime;

    public NotificationResponse(Notification notification, int zoneId) {
        text = notification.getText();
        createDatetime = LocalDateTime
                .ofEpochSecond(notification.getCreateDatetime(), 0,
                ZoneOffset.ofHours(zoneId).getRules().getOffset(Instant.now()))
                .toString();
    }
}
