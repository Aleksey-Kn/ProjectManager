package ru.manager.ProgectManager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.services.PhotoService;
import ru.manager.ProgectManager.services.kanban.KanbanService;
import ru.manager.ProgectManager.services.project.ProjectService;
import ru.manager.ProgectManager.services.user.UserService;

import javax.servlet.http.HttpServletResponse;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/photo")
@RequiredArgsConstructor
public class PhotoController {
    private final UserService userService;
    private final ProjectService projectService;
    private final PhotoService photoService;
    private final KanbanService kanbanService;

    @Operation(summary = "Загрузка фотографии профиля пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фотография профиля пользователя", content = {
                    @Content(mediaType = "image/jpeg")
            }),
            @ApiResponse(responseCode = "404", description = "Указанного пользователя не существует"),
            @ApiResponse(responseCode = "406", description = "Сервер не имеет возможности передать данный контент")
    })
    @GetMapping("/user")
    public ResponseEntity<?> findUserImage(@RequestParam long id, HttpServletResponse response) {
        try {
            return photoService.sendFile(response, userService.findPhoto(id));
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Загрузка фотографии профиля проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фотография профиля проекта", content = {
                    @Content(mediaType = "image/jpeg")
            }),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует"),
            @ApiResponse(responseCode = "406", description = "Сервер не имеет возможности передать данный контент")
    })
    @GetMapping("/project")
    public ResponseEntity<?> findProjectImage(@RequestParam long id, HttpServletResponse response) {
        try {
            return photoService.sendFile(response, projectService.findPhoto(id));
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Загрузка картинки профиля канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фотография профиля канбана", content = {
                    @Content(mediaType = "image/jpeg")
            }),
            @ApiResponse(responseCode = "404", description = "Указанного канбана не существует"),
            @ApiResponse(responseCode = "406", description = "Сервер не имеет возможности передать данный контент")
    })
    @GetMapping("/kanban")
    public ResponseEntity<?> findKanbanImage(@RequestParam long id, HttpServletResponse response) {
        try {
            return photoService.sendFile(response, kanbanService.findImage(id));
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
