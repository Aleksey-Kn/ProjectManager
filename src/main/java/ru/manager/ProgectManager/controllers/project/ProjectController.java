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
import ru.manager.ProgectManager.DTO.request.PhotoDTO;
import ru.manager.ProgectManager.DTO.request.ProjectDataRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanMainDataResponse;
import ru.manager.ProgectManager.DTO.response.project.ProjectResponseWithFlag;
import ru.manager.ProgectManager.DTO.response.user.PublicMainUserDataResponse;
import ru.manager.ProgectManager.DTO.response.user.UserDataWithProjectRoleResponse;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.enums.TypeRoleProject;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.project.NoSuchProjectException;
import ru.manager.ProgectManager.services.kanban.KanbanService;
import ru.manager.ProgectManager.services.project.ProjectRoleService;
import ru.manager.ProgectManager.services.project.ProjectService;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Создание, изменение и удаление проекта")
public class ProjectController {
    private final ProjectService projectService;
    private final KanbanService kanbanService;
    private final ProjectRoleService roleService;

    @Operation(summary = "Создание проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Имя проекта не должно быть пустым", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Идентификатор созданного проекта", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdResponse.class))
            })
    })
    @PostMapping("/project")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> addProject(@RequestBody @Valid ProjectDataRequest request, BindingResult bindingResult,
                                        Principal principal) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS), HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(projectService.addProject(request, principal.getName()));
        }
    }

    @Operation(summary = "Получение данных проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не является участником проекта"),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProjectResponseWithFlag.class))
            })
    })
    @GetMapping("/project")
    public ProjectResponseWithFlag findProject(@RequestParam long id, Principal principal) throws ForbiddenException, NoSuchProjectException {
        return projectService.findProject(id, principal.getName());
    }

    @Operation(summary = "Изменение проекта", description = "Установление нового имени проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращание к несуществующему проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не является администратором проекта"),
            @ApiResponse(responseCode = "400", description = "Имя проекта не должно быть пустым", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Название проекта изменено")
    })
    @PutMapping("/project")
    public ResponseEntity<?> setData(@RequestParam long id, @RequestBody @Valid ProjectDataRequest request,
                                     BindingResult bindingResult, Principal principal)
            throws ForbiddenException, NoSuchProjectException {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            projectService.setData(id, request, principal.getName());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @Operation(summary = "Добавление картинки проекта", description = "Прикрепление картинки или замена существующей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращание к несуществующему проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не является администратором проекта"),
            @ApiResponse(responseCode = "400", description = "Файл не может быть прочитан", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Картинка сжата и сохранена")
    })
    @PostMapping("/project/photo")
    public void setPhoto(@RequestParam long id, @ModelAttribute PhotoDTO photoDTO, Principal principal)
            throws IOException, ForbiddenException, NoSuchProjectException {
        projectService.setPhoto(id, photoDTO.getFile(), principal.getName());
    }

    @Operation(summary = "Получение списка канбанов проекта",
            description = "Отображаются только канбаны, доступные пользователю в соответствии с его ролью")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращание к несуществующему проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к указанному проекту"),
            @ApiResponse(responseCode = "200",
                    description = "Список канбан досок в данном проекте, доступных текщему пользователю", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanMainDataResponse[].class))
            })
    })
    @GetMapping("/project/kanbans")
    public KanbanMainDataResponse[] allKanbanOfThisUser(@RequestParam @Parameter(description = "Идентификатор проекта") long id,
                                                        Principal principal)
            throws ForbiddenException, NoSuchProjectException {
        return kanbanService.findAllKanban(id, principal.getName());
    }

    @Operation(summary = "Поиск канбанов по имени в указанном проекте",
            description = "Отображаются только канбаны, доступные пользователю в соответствии с его ролью")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращание к несуществующему проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к указанному проекту"),
            @ApiResponse(responseCode = "200",
                    description = "Список канбан досок в данном проекте, найденных по данному названию", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanMainDataResponse[].class))
            })
    })
    @GetMapping("/project/kanbans_by_name")
    public KanbanMainDataResponse[] allKanbanOfThisUser(@RequestParam @Parameter(description = "Идентификатор проекта") long id,
                                                        @RequestParam String name, Principal principal)
            throws ForbiddenException, NoSuchProjectException {
        return kanbanService.findKanbansByName(id, name, principal.getName());
    }

    @Operation(summary = "Получение списка участников проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращание к несуществующему проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к указанному проекту"),
            @ApiResponse(responseCode = "200", description = "Список участников проекта", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDataWithProjectRoleResponse[].class))
            })
    })
    @GetMapping("/project/users")
    public UserDataWithProjectRoleResponse[] allParticipants(@RequestParam @Parameter(description = "Идентификатор проекта") long id,
                                                             Principal principal)
            throws ForbiddenException, NoSuchProjectException {
        return projectService.findAllMembers(id, principal.getName()).toArray(UserDataWithProjectRoleResponse[]::new);
    }

    @Operation(summary = "Поиск участников проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращание к несуществующему проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к указанному проекту"),
            @ApiResponse(responseCode = "200", description = "Список найденных участников проекта", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDataWithProjectRoleResponse[].class))
            })
    })
    @GetMapping("/project/users/find")
    public UserDataWithProjectRoleResponse[] findMembers(@RequestParam @Parameter(description = "Идентификатор проекта") long id,
                                                         @RequestParam @Parameter(description = "Имя или почта пользователя") String name,
                                                         Principal principal) throws ForbiddenException, NoSuchProjectException {
        return projectService.findMembersByNicknameOrEmail(id, name, principal.getName())
                .toArray(UserDataWithProjectRoleResponse[]::new);
    }

    @Operation(summary = "Получение списка участников проекта с указанной ролью")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращание к несуществующему проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к указанному проекту"),
            @ApiResponse(responseCode = "200", description = "Список участников проекта с указанной ролью", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PublicMainUserDataResponse[].class))
            })
    })
    @GetMapping("/project/users/role")
    public PublicMainUserDataResponse[] findParticipantsOnRole(@RequestParam long projectId, @RequestParam TypeRoleProject type,
                                                               @RequestParam(required = false) long roleId,
                                                               @RequestParam @Parameter(description = "Никнейм или почта искомого пользователя") String name,
                                                               Principal principal)
            throws ForbiddenException, NoSuchProjectException {
        return roleService.findUsersOnRole(type, roleId, projectId, name, principal.getName());
    }

    @Operation(summary = "Удаление проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращание к несуществующему проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не является администратором проекта"),
            @ApiResponse(responseCode = "200", description = "Удаление прошло успешно")
    })
    @DeleteMapping("/project")
    public void deleteProject(@RequestParam long id, Principal principal) throws ForbiddenException, NoSuchProjectException {
        projectService.deleteProject(id, principal.getName());
    }
}
