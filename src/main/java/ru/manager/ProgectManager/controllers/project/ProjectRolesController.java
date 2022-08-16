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
import ru.manager.ProgectManager.DTO.response.accessProject.CustomProjectRoleResponse;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.documents.NoSuchPageException;
import ru.manager.ProgectManager.exception.kanban.NoSuchKanbanException;
import ru.manager.ProgectManager.exception.project.NoSuchCustomRoleException;
import ru.manager.ProgectManager.exception.project.NoSuchProjectException;
import ru.manager.ProgectManager.exception.user.NoSuchUserException;
import ru.manager.ProgectManager.services.project.ProjectRoleService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;

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
                            schema = @Schema(implementation = CustomProjectRoleResponse[].class))
            }),
            @ApiResponse(responseCode = "403",
                    description = "Пользователь не имеет достаточных прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping
    public CustomProjectRoleResponse[] findAllCustomRole(@RequestParam @Parameter(description = "Идентификатор проекта") long id,
                                                         Principal principal) throws ForbiddenException, NoSuchProjectException {
        return projectRoleService.findAllCustomProjectRole(id, principal.getName());
    }

    @Operation(summary = "Добавление новой роли в проект")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Новая роль успешно создана", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdResponse.class))
            }),
            @ApiResponse(responseCode = "403",
                    description = "Пользователь не имеет достаточных прав доступа для совершения данного действия"),
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
                                     Principal principal)
            throws ForbiddenException, NoSuchProjectException, NoSuchPageException, NoSuchKanbanException {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            Optional<IdResponse> customProjectRole = projectRoleService.createCustomRole(request, principal.getName());
            if (customProjectRole.isPresent()) {
                return ResponseEntity.ok(customProjectRole.get());
            } else {
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
    public void deleteRole(@RequestParam long id, Principal principal)
            throws ForbiddenException, NoSuchProjectException {
        projectRoleService.deleteCustomRole(id, principal.getName());
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
                                          Principal principal)
            throws NoSuchCustomRoleException, ForbiddenException, NoSuchProjectException, NoSuchUserException {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            projectRoleService.editUserRole(request, principal.getName());
            return new ResponseEntity<>(HttpStatus.OK);
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
                                    BindingResult bindingResult, Principal principal)
            throws NoSuchCustomRoleException, ForbiddenException {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            if (projectRoleService.rename(id, nameRequest.getName(), principal.getName())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
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
    public void editCanCreateOrDeletePrivilege(@RequestParam long id,
                                               @RequestParam boolean canCreateOrDelete, Principal principal)
            throws NoSuchCustomRoleException, ForbiddenException {
        projectRoleService.putCanEditResource(id, canCreateOrDelete, principal.getName());
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
                                                  BindingResult bindingResult, Principal principal)
            throws NoSuchCustomRoleException, ForbiddenException, NoSuchKanbanException {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.FIELD_MUST_BE_NOT_NULL), HttpStatus.BAD_REQUEST);
        } else {
            projectRoleService.putKanbanConnections(putConnections, principal.getName());
            return new ResponseEntity<>(HttpStatus.OK);
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
                                                BindingResult bindingResult, Principal principal)
            throws NoSuchCustomRoleException, ForbiddenException, NoSuchPageException {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.FIELD_MUST_BE_NOT_NULL), HttpStatus.BAD_REQUEST);
        } else {
            projectRoleService.putPageConnections(putConnections, principal.getName());
            return new ResponseEntity<>(HttpStatus.OK);
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
                                                    BindingResult bindingResult, Principal principal)
            throws NoSuchCustomRoleException, ForbiddenException {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.FIELD_MUST_BE_NOT_NULL), HttpStatus.BAD_REQUEST);
        } else {
            projectRoleService.deleteKanbanConnectors(deleteConnect, principal.getName());
            return new ResponseEntity<>(HttpStatus.OK);
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
                                                  BindingResult bindingResult, Principal principal)
            throws NoSuchCustomRoleException, ForbiddenException {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.FIELD_MUST_BE_NOT_NULL), HttpStatus.BAD_REQUEST);
        } else {
            projectRoleService.deletePageConnectors(deleteConnect, principal.getName());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }
}
