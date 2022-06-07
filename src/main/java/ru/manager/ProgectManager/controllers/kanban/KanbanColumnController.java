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
import ru.manager.ProgectManager.DTO.request.kanban.DelayRemoveRequest;
import ru.manager.ProgectManager.DTO.request.kanban.KanbanColumnRequest;
import ru.manager.ProgectManager.DTO.request.kanban.SortColumnRequest;
import ru.manager.ProgectManager.DTO.request.kanban.TransportColumnRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanColumnResponse;
import ru.manager.ProgectManager.components.ErrorResponseEntityConfigurator;
import ru.manager.ProgectManager.entitys.kanban.KanbanColumn;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.kanban.KanbanColumnService;
import ru.manager.ProgectManager.services.user.UserService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/kanban/column")
@Tag(name = "Манипуляции с колонками канбан-доски")
public class KanbanColumnController {
    private final KanbanColumnService kanbanColumnService;
    private final ErrorResponseEntityConfigurator entityConfigurator;
    private final UserService userService;

    @Operation(summary = "Добавление колонки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему канбану", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "400", description = "Название колонки не должно быть пустым", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Идентификатор добавленной колонки", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdResponse.class))
            })
    })
    @PostMapping()
    public ResponseEntity<?> addColumn(@RequestBody @Valid KanbanColumnRequest kanbanColumnRequest,
                                       BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        }
        try {
            String login = principal.getName();
            Optional<KanbanColumn> column = kanbanColumnService.addColumn(kanbanColumnRequest, login);
            if (column.isPresent()) {
                return ResponseEntity.ok(new IdResponse(column.get().getId()));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Получение информации о колонке и её содержимом")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующей колонке", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Запрашиваемая колонка", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanColumnResponse.class))
            })
    })
    @GetMapping()
    public ResponseEntity<?> findColumn(@RequestParam @Parameter(description = "Идентификатор колонки") long id,
                                        @RequestParam int pageIndex, @RequestParam int rowCount, Principal principal) {
        try {
            String login = principal.getName();
            Optional<KanbanColumn> kanbanColumn = kanbanColumnService.findKanbanColumn(id, login);
            if (kanbanColumn.isPresent()) {
                return ResponseEntity.ok(new KanbanColumnResponse(kanbanColumn.get(), pageIndex, rowCount,
                        userService.findZoneIdForThisUser(login)));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COLUMN), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Изменение колонки", description = "Изменение названия колонки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующей колонке", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "400", description = "Переданные данные неприемлемы", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Название упешно изменено")
    })
    @PutMapping()
    public ResponseEntity<?> renameColumn(@RequestParam long id, @RequestBody @Valid NameRequest name,
                                          BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            try {
                String login = principal.getName();
                Optional<KanbanColumn> column = kanbanColumnService.renameColumn(id, name.getName(), login);
                if (column.isPresent()) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COLUMN), HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Удаление колонки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующей колонке", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Колонка успешно удалена")
    })
    @DeleteMapping()
    public ResponseEntity<?> removeColumn(@RequestParam long id, Principal principal) {
        try {
            String login = principal.getName();
            if (kanbanColumnService.deleteColumn(id, login)) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COLUMN), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Перемещение столбца канбана",
            description = "Изменение местоположения столбца на указанный порядковый номер")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Неверный идентификатор колонки", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "400", description = "Введённый желаемый порядковый номер колонки недопостим",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Указанный столбец успешно перенесён")
    })
    @PutMapping("/transport")
    public ResponseEntity<?> transportColumn(@RequestBody @Valid TransportColumnRequest transportColumnRequest,
                                             BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            String login = principal.getName();
            try {
                if (kanbanColumnService.transportColumn(transportColumnRequest, login)) {
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COLUMN),
                        HttpStatus.NOT_FOUND);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.INDEX_MORE_COLLECTION_SIZE),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Operation(summary = "Сортировка элементов столбца канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Неверный идентификатор колонки", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Отсортирванная по указанному ключу колонка", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanColumnResponse.class))
            })
    })
    @PostMapping("/sort")
    public ResponseEntity<?> sortColumn(@RequestBody SortColumnRequest sortColumnRequest, @RequestParam int pageIndex,
                                        @RequestParam int rowCount, Principal principal) {
        try {
            String login = principal.getName();
            Optional<KanbanColumn> kanbanColumn = kanbanColumnService
                    .sortColumn(sortColumnRequest, login);
            if (kanbanColumn.isPresent()) {
                return ResponseEntity.ok(new KanbanColumnResponse(kanbanColumn.get(), pageIndex, rowCount,
                        userService.findZoneIdForThisUser(login)));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COLUMN), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Установка интервала очитки столбца в днях")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Неверный идентификатор колонки", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "400",
                    description = "Передынный интервал очистки является отрицательным числом", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Установка интервала удаления произошла успешно")
    })
    @PostMapping("/delay")
    public ResponseEntity<?> setDelayRemover(@RequestBody @Valid DelayRemoveRequest request, BindingResult bindingResult,
                                             Principal principal) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.COUNT_MUST_BE_MORE_1), HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (kanbanColumnService.setDelayDeleter(request.getId(), request.getDelay(), principal.getName())) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COLUMN), HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Отключение автоматической очистки столбца")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Неверный идентификатор колонки", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Отключение автоматической очистки столбца прошло успешно")
    })
    @DeleteMapping("/delay")
    public ResponseEntity<?> deleteDelayRemover(@RequestParam @Parameter(description = "Идентификатор колонки") long id,
                                                Principal principal) {
        try {
            if (kanbanColumnService.removeDelayDeleter(id, principal.getName())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COLUMN), HttpStatus.NOT_FOUND);
        }
    }
}
