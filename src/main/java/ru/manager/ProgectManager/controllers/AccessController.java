package ru.manager.ProgectManager.controllers;

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
import ru.manager.ProgectManager.DTO.request.accessProject.CreateCustomRoleRequest;
import ru.manager.ProgectManager.DTO.request.accessProject.EditUserRoleRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.DTO.response.accessProject.AccessProjectResponse;
import ru.manager.ProgectManager.DTO.response.accessProject.CustomProjectRoleResponseList;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.accessProject.AccessProject;
import ru.manager.ProgectManager.entitys.accessProject.CustomProjectRole;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.NoSuchResourceException;
import ru.manager.ProgectManager.services.AccessProjectService;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;


@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Предоставление доступа к проекту и манипулирование ролями внутри проекта",
        description = "Позволяет приглашать пользователя в проект с помощью токена доступа, а так же изменять уровни доступа участников к ресурсам проекта")
public class AccessController {
    private final AccessProjectService accessProjectService;
    private final JwtProvider provider;

    @Operation(summary = "Получение всех кастомных ролей проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список кастомных ролей", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomProjectRoleResponseList.class))
            }),
            @ApiResponse(responseCode = "403",
                    description = "Пользователь не имеет достаточных прав доступа для совершения даннного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/roles")
    public ResponseEntity<?> findAllCustomRole(@RequestParam @Parameter(description = "Идентификатор проекта") long id) {
        try {
            Optional<Set<CustomProjectRole>> roles = accessProjectService
                    .findAllCustomProjectRole(id, provider.getLoginFromToken());
            if (roles.isPresent()) {
                return ResponseEntity.ok(new CustomProjectRoleResponseList(roles.get()));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Добавление новой роли в проект")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Новая роль успешно создана", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdResponse.class))
            }),
            @ApiResponse(responseCode = "403",
                    description = "Пользователь не имеет достаточных прав доступа для совершения даннного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта или канбана не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Название роли должно содержать видимые символы",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    })
    })
    @PostMapping("/role")
    public ResponseEntity<?> addRole(@RequestBody @Valid CreateCustomRoleRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                Optional<CustomProjectRole> customProjectRole =
                        accessProjectService.createCustomRole(request, provider.getLoginFromToken());
                if (customProjectRole.isPresent()) {
                    return new ResponseEntity<>(new IdResponse(customProjectRole.get().getId()), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
            } catch (NoSuchResourceException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN), HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Удаление кастомной роли")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Роль успешно удалена"),
            @ApiResponse(responseCode = "403",
                    description = "Пользователь не имеет достаточных прав доступа для совершения даннного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта или роли не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @DeleteMapping("/role")
    public ResponseEntity<?> deleteRole(@RequestParam long projectId, @RequestParam long roleId) {
        try {
            if (accessProjectService.deleteCustomRole(projectId, roleId, provider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CUSTOM_ROLE), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Редактирование существующей роли")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Роль успешно изменена"),
            @ApiResponse(responseCode = "403",
                    description = "Пользователь не имеет достаточных прав доступа для совершения даннного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта, канбана или роли не существует",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Название роли должно содержать видимые символы",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    })
    })
    @PutMapping("/role")
    public ResponseEntity<?> editRole(@RequestBody @Valid CreateCustomRoleRequest request, BindingResult bindingResult,
                                      @RequestParam long roleId) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (accessProjectService.changeRole(roleId, request, provider.getLoginFromToken())) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
            } catch (NoSuchResourceException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN), HttpStatus.NOT_FOUND);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CUSTOM_ROLE),
                        HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Изменение роли участиника проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Указанная роль успешно применена к участнику"),
            @ApiResponse(responseCode = "403",
                    description = "Пользователь не имеет достаточных прав доступа для совершения даннного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта, участника или роли не существует",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Не задан тип роли пользователя",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    })
    })
    @PutMapping("/edit_user_role")
    public ResponseEntity<?> editUserRole(@RequestBody @Valid EditUserRoleRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (accessProjectService.editUserRole(request, provider.getLoginFromToken())) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
            } catch (NoSuchResourceException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_USER), HttpStatus.NOT_FOUND);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CUSTOM_ROLE),
                        HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Получение доступа",
            description = "Предоставляет доступ к проекту, к которому относится токен, пользователю, перешедшуму по данной ссылке")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Некорректный или устаревший токен доступа к проекту",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Подключение к проекту произошло успешно")
    })
    @GetMapping("/access")
    public ResponseEntity<?> getAccess(@RequestParam String token) {
        try {
            if (accessProjectService.createAccessForUser(token, provider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(
                    new ErrorResponse(Errors.PROJECT_ACCESS_TOKEN_IS_DEPRECATED),
                    HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.PROJECT_ACCESS_TOKEN_IS_INVALID_OR_NO_LONGER_AVAILABLE),
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
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Роль пользователя не указана", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "406",
                    description = "Попытка создать многоразовую ссылку для приглашения пользователя, как администратора",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Создание токена доступа произошло успешно", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccessProjectResponse.class))})
    })
    @PostMapping("/access")
    public ResponseEntity<?> postAccess(@RequestBody @Valid AccessProjectRequest accessProjectRequest,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                Optional<AccessProject> accessProject = accessProjectService.generateTokenForAccessProject(provider.getLoginFromToken(),
                        accessProjectRequest.getProjectId(), accessProjectRequest.getTypeRoleProject(),
                        accessProjectRequest.getRoleId(), accessProjectRequest.isDisposable(),
                        accessProjectRequest.getLiveTimeInDays());
                return accessProject.map(s -> ResponseEntity.ok(new AccessProjectResponse(s)))
                        .orElseGet(() -> new ResponseEntity<>(HttpStatus.FORBIDDEN));
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT),
                        HttpStatus.NOT_FOUND);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(
                        new ErrorResponse(Errors.TOKEN_FOR_ACCESS_WITH_PROJECT_AS_ADMIN_MUST_BE_DISPOSABLE),
                        HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }
}
