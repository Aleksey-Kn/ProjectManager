package ru.manager.ProgectManager.controllers.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.DTO.response.user.HasNewResponse;
import ru.manager.ProgectManager.DTO.response.user.notification.NotificationsResponseList;
import ru.manager.ProgectManager.components.authorization.JwtProvider;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.services.user.NotificationService;
import ru.manager.ProgectManager.services.user.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/user/notification")
@Tag(name = "Уведомления")
public class NotificationController {
    private final UserService userService;
    private final NotificationService notificationService;
    private final JwtProvider provider;

    @Operation(summary = "Получение информации о наличии непрочитанных уведомлений")
    @ApiResponse(responseCode = "200", description = "Наличие непрочитанных уведомлений", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = HasNewResponse.class))
    })
    @GetMapping("/has_new")
    public HasNewResponse readAllNotification() {
        return new HasNewResponse(notificationService.hasNewNotification(provider.getLoginFromToken()));
    }

    @Operation(summary = "Получение уведомлений", description = "Полученные уведомления помечаются как прочитанные")
    @ApiResponse(responseCode = "200", description = "Уведомления текущего пользователя", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = NotificationsResponseList.class))
    })
    @GetMapping("/read")
    public NotificationsResponseList findAllNotifications() {
        String login = provider.getLoginFromToken();
        User user = userService.findByUsername(login).orElseThrow();
        NotificationsResponseList notificationsResponseList =
                new NotificationsResponseList(user.getNotifications(), user.getZoneId());
        notificationService.readNotification(login);
        return notificationsResponseList;
    }
}
