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
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.NameRequest;
import ru.manager.ProgectManager.DTO.request.kanban.*;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanColumnResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.kanban.KanbanColumn;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.kanban.KanbanColumnService;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/kanban/column")
@Tag(name = "Манипуляции с колонками канбан-доски")
public class KanbanColumnController {
    private final KanbanColumnService kanbanColumnService;
    private final JwtProvider provider;

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
            @ApiResponse(responseCode = "200", description = "Добавленная колонка", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanColumn.class))
            })
    })
    @PostMapping("/add")
    public ResponseEntity<?> addColumn(@RequestBody @Valid KanbanColumnRequest kanbanColumnRequest,
                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return dropErrorResponse(bindingResult);
        }
        try {
            String login = provider.getLoginFromToken();
            Optional<KanbanColumn> column = kanbanColumnService.addColumn(kanbanColumnRequest, login);
            if (column.isPresent()) {
                return ResponseEntity.ok(column.get());
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
            @ApiResponse(responseCode = "400", description = "Переменные для пагинации должны быть положительными",
                    content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Запрашиваемая колонка", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanColumnResponse.class))
            })
    })
    @GetMapping("/get")
    public ResponseEntity<?> findColumn(@RequestBody @Valid GetKanbanColumnRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return dropErrorResponse(bindingResult);
        } else {
            try {
                Optional<KanbanColumn> kanbanColumn = kanbanColumnService.findKanbanColumn(request.getId(),
                        provider.getLoginFromToken());
                if (kanbanColumn.isPresent()) {
                    return ResponseEntity.ok(new KanbanColumnResponse(kanbanColumn.get(), request.getPageIndex(),
                            request.getCount()));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COLUMN), HttpStatus.NOT_FOUND);
            }
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
    @PutMapping("/put")
    public ResponseEntity<?> renameColumn(@RequestParam long id, @RequestBody @Valid NameRequest name,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return dropErrorResponse(bindingResult);
        } else {
            try {
                String login = provider.getLoginFromToken();
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
    @DeleteMapping("/delete")
    public ResponseEntity<?> removeColumn(@RequestParam long id) {
        try {
            String login = provider.getLoginFromToken();
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
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return dropErrorResponse(bindingResult);
        } else {
            String login = provider.getLoginFromToken();
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
                                        @RequestParam int rowCount) {
        try {
            Optional<KanbanColumn> kanbanColumn = kanbanColumnService
                    .sortColumn(sortColumnRequest, provider.getLoginFromToken());
            if (kanbanColumn.isPresent()) {
                return ResponseEntity.ok(new KanbanColumnResponse(kanbanColumn.get(), pageIndex, rowCount));
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
    public ResponseEntity<?> setDelayRemover(@RequestBody @Valid DelayRemoveRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.COUNT_MUST_BE_MORE_1), HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (kanbanColumnService.setDelayDeleter(request.getId(), request.getDelay(),
                        provider.getLoginFromToken())) {
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
    public ResponseEntity<?> deleteDelayRemover(@RequestParam @Parameter(description = "Идентификатор колонки") long id) {
        try {
            if (kanbanColumnService.removeDelayDeleter(id, provider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COLUMN), HttpStatus.NOT_FOUND);
        }
    }

    private ResponseEntity<?> dropErrorResponse(BindingResult bindingResult) {
        return new ResponseEntity<>(new ErrorResponse(bindingResult.getAllErrors().stream()
                        .map(ObjectError::getDefaultMessage)
                        .map(Errors::valueOf)
                        .map(Errors::getNumValue)
                        .collect(Collectors.toList())),
                HttpStatus.BAD_REQUEST);
    }
}
