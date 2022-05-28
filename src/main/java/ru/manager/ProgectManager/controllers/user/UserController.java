package ru.manager.ProgectManager.controllers.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.manager.ProgectManager.DTO.request.NameRequest;
import ru.manager.ProgectManager.DTO.request.user.LocaleRequest;
import ru.manager.ProgectManager.DTO.request.user.UpdatePassRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.ListPointerResources;
import ru.manager.ProgectManager.DTO.response.project.ProjectListResponse;
import ru.manager.ProgectManager.DTO.response.user.MyselfUserDataResponse;
import ru.manager.ProgectManager.DTO.response.user.PublicAllDataResponse;
import ru.manager.ProgectManager.DTO.response.user.VisitMarkListResponse;
import ru.manager.ProgectManager.components.ErrorResponseEntityConfigurator;
import ru.manager.ProgectManager.components.authorization.JwtProvider;
import ru.manager.ProgectManager.entitys.Project;
import ru.manager.ProgectManager.entitys.user.User;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.NoteService;
import ru.manager.ProgectManager.services.project.AccessProjectService;
import ru.manager.ProgectManager.services.user.UserService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/user")
@Tag(name = "Управление аккаунтом пользователя")
public class UserController {
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final AccessProjectService accessProjectService;
    private final ErrorResponseEntityConfigurator entityConfigurator;
    private final NoteService noteService;

    @Operation(summary = "Предоставление информации о текущем пользователе")
    @ApiResponse(responseCode = "200", description = "Возвращение информации о профиле", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MyselfUserDataResponse.class))
    })
    @GetMapping("/current")
    public ResponseEntity<?> findMyselfAccountData() {
        return ResponseEntity.ok(new MyselfUserDataResponse(userService.findByUsername(jwtProvider.getLoginFromToken())
                .orElseThrow()));
    }

    @Operation(summary = "Предоставление информации об указанном пользователе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к неуществующему пользователю", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200",
                    description = "Возвращение информации о профиле", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PublicAllDataResponse.class))
            })
    })
    @GetMapping("/other")
    public ResponseEntity<?> findOtherAccountData(@RequestParam @Parameter(description = "Идентификатор искомого пользователя")
                                                          long id) {
        Optional<User> targetUser = userService.findById(id);
        String nowLogin = jwtProvider.getLoginFromToken();
        if (targetUser.isPresent()) {
            return ResponseEntity.ok(new PublicAllDataResponse(targetUser.get(),
                    userService.findZoneIdForThisUser(nowLogin),
                    noteService.findNote(id, nowLogin)));
        } else {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_USER), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Изменение отображаемого имени аккаунта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Имя пользователя должно содержать видимые символы",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Имя пользователя успешно изменено")
    })
    @PutMapping()
    public ResponseEntity<?> rename(@RequestBody @Valid NameRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            userService.renameUser(jwtProvider.getLoginFromToken(), request.getName());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @Operation(summary = "Изменение пароля аккаунта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Неприемлемый пароль",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Пароль успешно изменён"),
            @ApiResponse(responseCode = "403", description = "Неверный текущий пароль",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
    })
    @PutMapping("/pass")
    public ResponseEntity<?> updatePass(@RequestBody @Valid UpdatePassRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            if (userService.updatePass(request.getOldPass(), request.getNewPass(), jwtProvider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_LOGIN_OR_PASSWORD), HttpStatus.FORBIDDEN);
            }
        }
    }

    @Operation(summary = "Изменение языка аккаунта пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Неприемлемый язык", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Язык успешно изменён")
    })
    @PutMapping("/locale")
    public ResponseEntity<?> updateLocale(@RequestBody @Valid LocaleRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.FIELD_MUST_BE_NOT_NULL), HttpStatus.BAD_REQUEST);
        } else {
            userService.updateLocale(request, jwtProvider.getLoginFromToken());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @Operation(summary = "Установление фотографии профиля", description = "Добавление или замена фотографии")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Файл не может быть корректно прочитан или обработан",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Фотография успешно сжата и сохранена")
    })
    @PostMapping("/photo")
    public ResponseEntity<?> setPhoto(@RequestParam("file") MultipartFile multipartFile) {
        try {
            userService.setPhoto(jwtProvider.getLoginFromToken(), multipartFile);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.BAD_FILE), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Список проектов, доступных для данного пользователя")
    @ApiResponse(responseCode = "200", description = "Список проектов, доступных пользователю", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProjectListResponse.class))
    })
    @GetMapping("/projects")
    public ProjectListResponse getUserProjects() {
        String login = jwtProvider.getLoginFromToken();
        List<Project> projectList = userService.allProjectOfThisUser(login);
        List<String> roles = new LinkedList<>();
        projectList.forEach(p -> roles.add(accessProjectService.findUserRoleName(login, p.getId())));
        return new ProjectListResponse(projectList, roles, userService.findZoneIdForThisUser(login));
    }

    @Operation(summary = "Результат поиска проектов по имени")
    @ApiResponse(responseCode = "200", description = "Список проектов, доступных пользователю, с фильтрацией по имени",
            content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProjectListResponse.class))
            })
    @GetMapping("/projects_by_name")
    public ProjectListResponse findProjectsByName(@RequestParam String name) {
        String login = jwtProvider.getLoginFromToken();
        List<Project> projects = userService.projectsByNameOfThisUser(name, login);
        List<String> roles = new LinkedList<>();
        projects.forEach(p -> roles.add(accessProjectService.findUserRoleName(login, p.getId())));
        return new ProjectListResponse(projects, roles, userService.findZoneIdForThisUser(login));
    }

    @Operation(summary = "Результат поиска ресурсов по имени")
    @ApiResponse(responseCode = "200", description = "Список ресурсов, доступных пользователю, с фильтрацией по имени",
            content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ListPointerResources.class))
            })
    @GetMapping("/resources")
    public ListPointerResources findResourcesByName(@RequestParam String name) {
        return new ListPointerResources(userService.availableResourceByName(name, jwtProvider.getLoginFromToken()));
    }

    @Operation(summary = "Список последних посещённых ресурсов пользователем")
    @ApiResponse(responseCode = "200", description = "Список ресурсов с сортировкой по времени поледнего посещения",
            content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = VisitMarkListResponse.class))
            })
    @GetMapping("/lasts")
    public VisitMarkListResponse findLastSee() {
        return new VisitMarkListResponse(userService.lastVisits(jwtProvider.getLoginFromToken()));
    }
}
