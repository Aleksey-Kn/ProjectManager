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
import ru.manager.ProgectManager.services.user.NotificationService;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/user/notification")
@Tag(name = "Уведомления")
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "Получение информации о наличии непрочитанных уведомлений")
    @ApiResponse(responseCode = "200", description = "Наличие непрочитанных уведомлений", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = HasNewResponse.class))
    })
    @GetMapping("/has_new")
    public HasNewResponse readAllNotification(Principal principal) {
        return new HasNewResponse(notificationService.hasNewNotification(principal.getName()));
    }

    @Operation(summary = "Получение уведомлений", description = "Полученные уведомления помечаются как прочитанные")
    @ApiResponse(responseCode = "200", description = "Уведомления текущего пользователя", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = NotificationsResponseList.class))
    })
    @GetMapping("/read")
    public NotificationsResponseList findAllNotifications(Principal principal) {
        String login = principal.getName();
        NotificationsResponseList notificationsResponseList = notificationService.findNotifications(login);
        notificationService.readNotification(login);
        return notificationsResponseList;
    }
}
