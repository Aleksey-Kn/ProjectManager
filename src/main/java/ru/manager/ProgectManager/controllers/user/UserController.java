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
import ru.manager.ProgectManager.DTO.response.project.ProjectResponse;
import ru.manager.ProgectManager.DTO.response.user.MyselfUserDataResponse;
import ru.manager.ProgectManager.DTO.response.user.PublicAllDataResponse;
import ru.manager.ProgectManager.DTO.response.user.VisitMarkResponse;
import ru.manager.ProgectManager.components.ErrorResponseEntityConfigurator;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.user.IncorrectLoginOrPasswordException;
import ru.manager.ProgectManager.exception.user.NoSuchUserException;
import ru.manager.ProgectManager.services.user.NoteService;
import ru.manager.ProgectManager.services.user.UserService;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/user")
@Tag(name = "Управление аккаунтом пользователя")
public class UserController {
    private final UserService userService;
    private final ErrorResponseEntityConfigurator entityConfigurator;
    private final NoteService noteService;

    @Operation(summary = "Предоставление информации о текущем пользователе")
    @ApiResponse(responseCode = "200", description = "Возвращение информации о профиле", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MyselfUserDataResponse.class))
    })
    @GetMapping("/current")
    public MyselfUserDataResponse findMyselfAccountData(Principal principal) {
        return userService.findMyselfUserDataResponseByUsername(principal.getName());
    }

    @Operation(summary = "Предоставление информации об указанном пользователе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему пользователю", content = {
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
    public PublicAllDataResponse findOtherAccountData(@RequestParam @Parameter(description = "Идентификатор искомого пользователя")
                                                      long id, Principal principal) throws NoSuchUserException {
        String nowLogin = principal.getName();
        var targetUser = userService.findById(id, nowLogin);
        targetUser.setNote(noteService.findNote(id, nowLogin));
        return targetUser;
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
    public ResponseEntity<?> rename(@RequestBody @Valid NameRequest request, BindingResult bindingResult,
                                    Principal principal) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            userService.renameUser(principal.getName(), request.getName());
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
    public ResponseEntity<?> updatePass(@RequestBody @Valid UpdatePassRequest request, BindingResult bindingResult,
                                        Principal principal) throws IncorrectLoginOrPasswordException {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            userService.updatePass(request.getOldPass(), request.getNewPass(), principal.getName());
            return new ResponseEntity<>(HttpStatus.OK);
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
    public ResponseEntity<?> updateLocale(@RequestBody @Valid LocaleRequest request, BindingResult bindingResult,
                                          Principal principal) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.FIELD_MUST_BE_NOT_NULL), HttpStatus.BAD_REQUEST);
        } else {
            userService.updateLocale(request, principal.getName());
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
    public void setPhoto(@RequestParam("file") MultipartFile multipartFile, Principal principal) throws IOException {
        userService.setPhoto(principal.getName(), multipartFile);
    }

    @Operation(summary = "Список проектов, доступных для данного пользователя")
    @ApiResponse(responseCode = "200", description = "Список проектов, доступных пользователю", content = {
            @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ProjectResponse[].class))
    })
    @GetMapping("/projects")
    public ProjectResponse[] getUserProjects(Principal principal) {
        return userService.allProjectOfThisUser(principal.getName()).toArray(ProjectResponse[]::new);
    }

    @Operation(summary = "Результат поиска проектов по имени")
    @ApiResponse(responseCode = "200", description = "Список проектов, доступных пользователю, с фильтрацией по имени",
            content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProjectResponse[].class))
            })
    @GetMapping("/projects_by_name")
    public ProjectResponse[] findProjectsByName(@RequestParam String name, Principal principal) {
        return userService.projectsByNameOfThisUser(name, principal.getName()).toArray(ProjectResponse[]::new);
    }

    @Operation(summary = "Результат поиска ресурсов по имени")
    @ApiResponse(responseCode = "200", description = "Список ресурсов, доступных пользователю, с фильтрацией по имени",
            content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ListPointerResources.class))
            })
    @GetMapping("/resources")
    public ListPointerResources findResourcesByName(@RequestParam String name, Principal principal) {
        return userService.availableResourceByName(name, principal.getName());
    }

    @Operation(summary = "Список последних посещённых ресурсов пользователем")
    @ApiResponse(responseCode = "200", description = "Список ресурсов с сортировкой по времени последнего посещения",
            content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = VisitMarkResponse[].class))
            })
    @GetMapping("/lasts")
    public VisitMarkResponse[] findLastSee(Principal principal) {
        return userService.lastVisits(principal.getName()).toArray(VisitMarkResponse[]::new);
    }
}
