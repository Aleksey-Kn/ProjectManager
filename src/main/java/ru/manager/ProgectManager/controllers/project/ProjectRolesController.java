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
import ru.manager.ProgectManager.DTO.request.NameRequest;
import ru.manager.ProgectManager.DTO.request.accessProject.CreateCustomRoleRequest;
import ru.manager.ProgectManager.DTO.request.accessProject.DeleteConnectForResourceFromRole;
import ru.manager.ProgectManager.DTO.request.accessProject.EditUserRoleRequest;
import ru.manager.ProgectManager.DTO.request.accessProject.PutConnectForResourceInRole;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.DTO.response.accessProject.CustomProjectRoleResponseList;
import ru.manager.ProgectManager.entitys.accessProject.CustomProjectRole;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.runtime.NoSuchResourceException;
import ru.manager.ProgectManager.services.project.ProjectRoleService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/roles")
@Tag(name = "Манипулирование ролями внутри проекта",
        description = "Позволяет изменять уровни доступа участников к ресурсам проекта")
public class ProjectRolesController {
    private final ProjectRoleService projectRoleService;

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
    @GetMapping()
    public ResponseEntity<?> findAllCustomRole(@RequestParam @Parameter(description = "Идентификатор проекта") long id,
                                               Principal principal) {
        try {
            Optional<Set<CustomProjectRole>> roles = projectRoleService
                    .findAllCustomProjectRole(id, principal.getName());
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
            @ApiResponse(responseCode = "404", description = "Указанного проекта или канбана, или документа не существует",
                    content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Название роли должно содержать видимые символы",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "409", description = "Роль с таким названием уже существует в проекте")
    })
    @PostMapping
    public ResponseEntity<?> addRole(@RequestBody @Valid CreateCustomRoleRequest request, BindingResult bindingResult,
                                     Principal principal) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                Optional<CustomProjectRole> customProjectRole =
                        projectRoleService.createCustomRole(request, principal.getName());
                if (customProjectRole.isPresent()) {
                    return new ResponseEntity<>(new IdResponse(customProjectRole.get().getId()), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
            } catch (NoSuchResourceException e) {
                if(e.getMessage().charAt(0) == 'K') {
                    return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN), HttpStatus.NOT_FOUND);
                } else {
                    return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PAGE), HttpStatus.NOT_FOUND);
                }
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }
    }

    @Operation(summary = "Удаление кастомной роли")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Роль успешно удалена"),
            @ApiResponse(responseCode = "403",
                    description = "Пользователь не имеет достаточных прав доступа для совершения даннного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного роли не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @DeleteMapping()
    public ResponseEntity<?> deleteRole(@RequestParam long id, Principal principal) {
        try {
            if (projectRoleService.deleteCustomRole(id, principal.getName())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
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
    public ResponseEntity<?> editUserRole(@RequestBody @Valid EditUserRoleRequest request, BindingResult bindingResult,
                                          Principal principal) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (projectRoleService.editUserRole(request, principal.getName())) {
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

    @Operation(summary = "Переименование роли")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Переименование прошло успешно"),
            @ApiResponse(responseCode = "400", description = "Название не должно быть пустым", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403",
                    description = "Пользователь не является администратором проекта, к которому относится данная роль"),
            @ApiResponse(responseCode = "404", description = "Указанной роли не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "409", description = "Роль с таким названием уже есть в проекте")
    })
    @PutMapping("/rename")
    public ResponseEntity<?> rename(@RequestParam long id, @RequestBody @Valid NameRequest nameRequest,
                                    BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (projectRoleService.rename(id, nameRequest.getName(), principal.getName())) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CUSTOM_ROLE),
                        HttpStatus.NOT_FOUND);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }
    }

    @Operation(summary =
            "Установка возможности сохранения и удаления ресурсов проекта пользователей, которые принадлежат к данной роли")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Изменение прошло успешно"),
            @ApiResponse(responseCode = "403",
                    description = "Пользователь не является администратором проекта, к которому относится данная роль"),
            @ApiResponse(responseCode = "404", description = "Указанной роли не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PutMapping("/can_edit")
    public ResponseEntity<?> editCanCreateOrDeletePrivilege(@RequestParam long id,
                                                            @RequestParam boolean canCreateOrDelete, Principal principal) {
        try {
            if (projectRoleService.putCanEditResource(id, canCreateOrDelete, principal.getName())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CUSTOM_ROLE),
                    HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Установка доступа к канбанам участников данной роли",
            description = "Служит как для добавления новых доступов, так и для изменения уже существующих")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Доступ предоставлен"),
            @ApiResponse(responseCode = "400", description = "Список предоставляемых ресурсов не должен отсутствовать",
                    content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403",
                    description = "Пользователь не является администратором проекта, к которому относится данная роль"),
            @ApiResponse(responseCode = "404", description = "Указанной роли или канбана не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PostMapping("/connection/kanban")
    public ResponseEntity<?> editKanbanConnection(@RequestBody @Valid PutConnectForResourceInRole putConnections,
                                                  BindingResult bindingResult, Principal principal) {
        if(bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.FIELD_MUST_BE_NOT_NULL), HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (projectRoleService.putKanbanConnections(putConnections, principal.getName())) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CUSTOM_ROLE),
                        HttpStatus.NOT_FOUND);
            } catch (NoSuchResourceException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN), HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Установка доступа к страницам документов участников данной роли",
            description = "Служит как для добавления новых доступов, так и для изменения уже существующих")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Доступ предоставлен"),
            @ApiResponse(responseCode = "400", description = "Список предоставляемых ресурсов не должен отсутствовать",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "403",
                    description = "Пользователь не является администратором проекта, к которому относится данная роль"),
            @ApiResponse(responseCode = "404", description = "Указанной роли или страницы не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PostMapping("/connection/page")
    public ResponseEntity<?> editPageConnection(@RequestBody @Valid PutConnectForResourceInRole putConnections,
                                                  BindingResult bindingResult, Principal principal) {
        if(bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.FIELD_MUST_BE_NOT_NULL), HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (projectRoleService.putPageConnections(putConnections, principal.getName())) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CUSTOM_ROLE),
                        HttpStatus.NOT_FOUND);
            } catch (NoSuchResourceException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PAGE), HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Удаление доступа к канбанам участников данной роли")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Доступ удалён"),
            @ApiResponse(responseCode = "400", description = "Список удаляемых подключений не должен отсутствовать",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "403",
                    description = "Пользователь не является администратором проекта, к которому относится данная роль"),
            @ApiResponse(responseCode = "404", description = "Указанной роли не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @DeleteMapping("/connection/kanban")
    public ResponseEntity<?> deleteKanbanConnection(@RequestBody @Valid DeleteConnectForResourceFromRole deleteConnect,
                                                  BindingResult bindingResult, Principal principal) {
        if(bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.FIELD_MUST_BE_NOT_NULL), HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (projectRoleService.deleteKanbanConnectors(deleteConnect, principal.getName())) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CUSTOM_ROLE),
                        HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Удаление доступа к страницам документов участников данной роли")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Доступ удалён"),
            @ApiResponse(responseCode = "400", description = "Список удаляемых подключений не должен отсутствовать",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "403",
                    description = "Пользователь не является администратором проекта, к которому относится данная роль"),
            @ApiResponse(responseCode = "404", description = "Указанной роли не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @DeleteMapping("/connection/page")
    public ResponseEntity<?> deletePageConnection(@RequestBody @Valid DeleteConnectForResourceFromRole deleteConnect,
                                                  BindingResult bindingResult, Principal principal) {
        if(bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.FIELD_MUST_BE_NOT_NULL), HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (projectRoleService.deletePageConnectors(deleteConnect, principal.getName())) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CUSTOM_ROLE),
                        HttpStatus.NOT_FOUND);
            }
        }
    }
}
