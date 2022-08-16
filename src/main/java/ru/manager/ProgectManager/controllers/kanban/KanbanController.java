package ru.manager.ProgectManager.controllers.kanban;

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
import ru.manager.ProgectManager.DTO.request.PhotoDTO;
import ru.manager.ProgectManager.DTO.request.kanban.TagRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanContentResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanMembers;
import ru.manager.ProgectManager.DTO.response.kanban.TagResponse;
import ru.manager.ProgectManager.components.ErrorResponseEntityConfigurator;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.kanban.NoSuchKanbanException;
import ru.manager.ProgectManager.exception.kanban.NoSuchTagException;
import ru.manager.ProgectManager.exception.project.NoSuchProjectException;
import ru.manager.ProgectManager.services.kanban.KanbanService;

import javax.validation.Valid;
import java.io.IOException;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/kanban")
@Tag(name = "Манипуляции с канбан-доской")
public class KanbanController {
    private final KanbanService kanbanService;
    private final ErrorResponseEntityConfigurator entityConfigurator;

    @Operation(summary = "Добавление новой канбан-доски в проект")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Указанного проекта не сущесвует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному действию"),
            @ApiResponse(responseCode = "400", description = "Имя не должно быть пустым",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdResponse.class))
            })
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createKanban(@RequestParam long projectId, @RequestBody @Valid NameRequest name,
                                          BindingResult bindingResult, Principal principal)
            throws ForbiddenException, NoSuchProjectException {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            return ResponseEntity.ok(kanbanService.createKanban(projectId, name.getName(), principal.getName()));
        }
    }

    @Operation(summary = "Переименование канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Указанного канбана не сущесвует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному действию"),
            @ApiResponse(responseCode = "400", description = "Имя не должно быть пустым", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Канбана-доска успешно переименована")
    })
    @PutMapping("/rename")
    public ResponseEntity<?> rename(@RequestParam @Parameter(description = "Идентификатор переименовываемого канбана") long id,
                                    @RequestBody @Valid NameRequest nameRequest, BindingResult bindingResult,
                                    Principal principal) throws ForbiddenException, NoSuchKanbanException {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            kanbanService.rename(id, nameRequest.getName(), principal.getName());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @Operation(summary = "Установление или обновление картинки канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Указанного канбана не сущесвует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Ошибка чтения файла", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному действию"),
            @ApiResponse(responseCode = "200", description = "Картинка успешно установлена")
    })
    @PostMapping("/image")
    public void setImage(@RequestParam @Parameter(description = "Идентификатор канбана") long id,
                         @ModelAttribute PhotoDTO photoDTO, Principal principal)
            throws ForbiddenException, IOException, NoSuchKanbanException {
        kanbanService.setImage(id, photoDTO.getFile(), principal.getName());
    }

    @Operation(summary = "Получение канбан-доски",
            description = "Получение данных канбана и идентификаторов его колонок")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Указанного канбана не сущесвует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к указанному ресурсу"),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanContentResponse.class))
            })
    })
    @GetMapping
    public KanbanContentResponse getKanban(@RequestParam @Parameter(description = "Идентификатор канбана") long id,
                                           @RequestParam int pageIndex, @RequestParam int rowCount, Principal principal)
            throws ForbiddenException, NoSuchKanbanException {
        return kanbanService.findKanban(id, principal.getName(), pageIndex, rowCount);
    }

    @Operation(summary = "Получение участников канбан-доски")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Указанного канбана не сущесвует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к указанному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Участники канбан-доски", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanMembers.class))
            })
    })
    @GetMapping("/members")
    public KanbanMembers findMembers(@RequestParam @Parameter(description = "Идентификатор канбана") long id,
                                     Principal principal) throws ForbiddenException, NoSuchKanbanException {
        return kanbanService.members(id, principal.getName());
    }

    @Operation(summary = "Удаление всей канбан-доски")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему канбану", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Канбан доска успешно удалена")
    })
    @DeleteMapping
    public void removeKanban(@RequestParam long id, Principal principal)
            throws ForbiddenException, NoSuchKanbanException {
        kanbanService.removeKanban(id, principal.getName());
    }

    @Operation(summary = "Получаение всех доступных для этого канбана тегов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему канбану", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Список тегов", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TagResponse[].class))
            })
    })
    @GetMapping("/tags")
    public TagResponse[] findAllTags(@RequestParam long id, Principal principal)
            throws ForbiddenException, NoSuchKanbanException {
        return kanbanService.findAllAvailableTags(id, principal.getName());
    }

    @Operation(summary = "Добавление тега в канбан")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему канбану", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Информация о добавленном теге", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdResponse.class))
            })
    })
    @PostMapping("/tag")
    @ResponseStatus(HttpStatus.CREATED)
    public IdResponse addTag(@RequestParam @Parameter(description = "Идентификатор канбана") long id,
                             @RequestBody TagRequest request, Principal principal)
            throws ForbiddenException, NoSuchKanbanException {
        return kanbanService.addTag(id, request, principal.getName());
    }

    @Operation(summary = "Удаление тега из канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему канбану или тегу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Тег успешно удалён из канбана")
    })
    @DeleteMapping("/tag")
    public void deleteTag(@RequestParam @Parameter(description = "Идентификатор тега") long id,
                          Principal principal) throws NoSuchTagException, ForbiddenException {
        kanbanService.removeTag(id, principal.getName());
    }

    @Operation(summary = "Изменение существующего тега")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему тегу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Информация о теге успешно изменена")
    })
    @PutMapping("/tag")
    public void editTag(@RequestParam @Parameter(description = "Идентификатор тега") long id,
                        @RequestBody TagRequest request, Principal principal)
            throws NoSuchTagException, ForbiddenException {
        kanbanService.editTag(id, request, principal.getName());
    }
}
