package ru.manager.ProgectManager.controllers.project;

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
import ru.manager.ProgectManager.DTO.request.accessProject.AccessProjectRequest;
import ru.manager.ProgectManager.DTO.request.accessProject.AccessProjectTroughMailRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.accessProject.AccessProjectResponse;
import ru.manager.ProgectManager.DTO.response.project.ProjectResponse;
import ru.manager.ProgectManager.components.ErrorResponseEntityConfigurator;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.project.AccessTokenInvalidException;
import ru.manager.ProgectManager.exception.project.NoSuchCustomRoleException;
import ru.manager.ProgectManager.exception.project.NoSuchProjectException;
import ru.manager.ProgectManager.exception.runtime.IllegalActionException;
import ru.manager.ProgectManager.exception.user.NoSuchUserException;
import ru.manager.ProgectManager.services.project.AccessProjectService;
import ru.manager.ProgectManager.services.user.UserService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/users/access")
@Tag(name = "Предоставление доступа к проекту",
        description = "Позволяет приглашать пользователя в проект с помощью токена доступа")
public class AccessController {
    private final AccessProjectService accessProjectService;
    private final ErrorResponseEntityConfigurator entityConfigurator;
    private final UserService userService;

    @ExceptionHandler(AccessTokenInvalidException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse accessTokenInvalidExceptionHandler() {
        return new ErrorResponse(Errors.PROJECT_ACCESS_TOKEN_IS_INVALID_OR_NO_LONGER_AVAILABLE);
    }

    @ExceptionHandler(IllegalActionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse lastAdminTryLeaveHandler() {
        return new ErrorResponse(Errors.LAST_ADMIN_CAN_NOT_LEAVE);
    }

    @Operation(summary = "Получение доступа",
            description = "Предоставляет доступ к проекту, к которому относится токен, пользователю, перешедшуму по данной ссылке")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Устаревший токен доступа к проекту",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Некорректный токен доступа к проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Подключение к проекту произошло успешно")
    })
    @GetMapping
    public ResponseEntity<?> getAccess(@RequestParam String token, Principal principal) throws AccessTokenInvalidException {
        if (accessProjectService.createAccessForUser(token, principal.getName())) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse(Errors.PROJECT_ACCESS_TOKEN_IS_DEPRECATED),
                    HttpStatus.FORBIDDEN);
        }
    }

    @Operation(summary = "Получение информации о проекте, к которому может быть предоставлен доступ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Устаревший токен доступа к проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Некорректный токен доступа к проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200",
                    description = "Информация о проекте, а также будущей роли пользователя в нём", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProjectResponse.class))
            })
    })
    @GetMapping("/info")
    public ResponseEntity<?> getInfo(@RequestParam String token, Principal principal) throws AccessTokenInvalidException {
        Optional<ProjectResponse> response = accessProjectService.findInfoOfProjectFromAccessToken(token,
                userService.findZoneIdForThisUser(principal.getName()));
        if (response.isPresent()) {
            return ResponseEntity.ok(response.get());
        } else {
            return new ResponseEntity<>(new ErrorResponse(Errors.PROJECT_ACCESS_TOKEN_IS_DEPRECATED),
                    HttpStatus.FORBIDDEN);
        }
    }

    @Operation(summary = "Предоставление доступа", description = "Генерирует токен доступа к проекту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403",
                    description = "Пользователь, пытающийся предоставить доступ, не является администратором проекта",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "404", description = "Указанного проекта или роли не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Некоректные входные данные", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "406",
                    description = "Приглашать пользователя с ролью администратора можно только через отправку приглашения на почту",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Создание токена доступа прошло успешно", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccessProjectResponse.class))})
    })
    @PostMapping("/create")
    public ResponseEntity<?> postAccess(@RequestBody @Valid AccessProjectRequest accessProjectRequest,
                                        BindingResult bindingResult, Principal principal) throws NoSuchCustomRoleException, ForbiddenException, NoSuchProjectException {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            Optional<AccessProjectResponse> response = accessProjectService.generateTokenForAccessProject(
                    principal.getName(),
                    accessProjectRequest.getProjectId(), accessProjectRequest.getTypeRoleProject(),
                    accessProjectRequest.getRoleId(), accessProjectRequest.isDisposable(),
                    accessProjectRequest.getLiveTimeInDays());
            if (response.isPresent()) {
                return ResponseEntity.ok(response.get());
            } else {
                return new ResponseEntity<>(
                        new ErrorResponse(Errors.TOKEN_FOR_ACCESS_WITH_PROJECT_AS_ADMIN_MUST_BE_DISPOSABLE),
                        HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }

    @Operation(summary = "Предоставление доступа через почту пользователя",
            description = "Отправляет реферальную ссылку на указанную почту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403",
                    description = "Пользователь, пытающийся предоставить доступ, не является администратором проекта",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "404", description = "Указанного проекта или роли не существует",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Отправка реферальной ссылки прошла успешно")
    })
    @PostMapping("/send")
    public ResponseEntity<?> sendAccessToMail(@RequestBody @Valid AccessProjectTroughMailRequest request,
                                              BindingResult bindingResult, Principal principal)
            throws NoSuchCustomRoleException, ForbiddenException, NoSuchProjectException {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            accessProjectService.sendInvitationToMail(request, principal.getName());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @Operation(summary = "Исключение себя из проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "400",
                    description = "Единственный существующий администратор не может выйти из проекта"),
            @ApiResponse(responseCode = "200", description = "Текущий пользователь исключён из проекта")
    })
    @PostMapping("/leave")
    public void leave(@RequestParam @Parameter(description = "Идентификатор покидаемого проекта") long id,
                      Principal principal) throws ForbiddenException, NoSuchProjectException {
        accessProjectService.leave(id, principal.getName());
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Пользователь, пытающийся удалить другого пользователя, " +
                    "не является администратором проекта или пытается исключить себя или другого администратора"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта или пользователя не существует",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Указанный пользователь исключён из проекта")
    })
    @DeleteMapping("/kick")
    public void kick(@RequestParam long projectId, @RequestParam long userId, Principal principal)
            throws ForbiddenException, NoSuchProjectException, NoSuchUserException {
        accessProjectService.kick(projectId, userId, principal.getName());
    }
}
