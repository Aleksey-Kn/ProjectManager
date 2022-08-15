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
import ru.manager.ProgectManager.DTO.request.kanban.CreateKanbanElementRequest;
import ru.manager.ProgectManager.DTO.request.kanban.TransportElementRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanElementContentResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanElementMainDataResponse;
import ru.manager.ProgectManager.components.ErrorResponseEntityConfigurator;
import ru.manager.ProgectManager.enums.ElementStatus;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.enums.SearchElementType;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.kanban.*;
import ru.manager.ProgectManager.services.kanban.KanbanElementAttributesService;
import ru.manager.ProgectManager.services.kanban.KanbanElementService;

import javax.validation.Valid;
import java.security.Principal;
import java.time.format.DateTimeParseException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/kanban/element")
@Tag(name = "Манипуляции с элементами канбан-доски")
public class KanbanElementController {
    private final KanbanElementService kanbanElementService;
    private final KanbanElementAttributesService attributesService;
    private final ErrorResponseEntityConfigurator entityConfigurator;

    @ExceptionHandler(DateTimeParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse dateTimeParseExceptionHandler() {
        return new ErrorResponse(Errors.WRONG_DATE_FORMAT);
    }

    @Operation(summary = "Получение элемента канбана", description = "Получение полной информации об элементе канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Указанного элемента не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Запрашивающий пользователь не имеет доступа к ресурсу",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanElementContentResponse.class))
            })
    })
    @GetMapping()
    public KanbanElementContentResponse getContent(@RequestParam long elementId, Principal principal)
            throws ForbiddenException, NoSuchKanbanElementException {
        return kanbanElementService.getContentFromElement(elementId, principal.getName());
    }

    @Operation(summary = "Добавление элемента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Указанной колонки не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к ресурсу"),
            @ApiResponse(responseCode = "400", description = "Попытка создать элемент с пустым именем", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Добавленный элемент", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdResponse.class))
            })
    })
    @PostMapping
    public ResponseEntity<?> addElement(@RequestBody @Valid CreateKanbanElementRequest request,
                                        BindingResult bindingResult, Principal principal)
            throws ForbiddenException, NoSuchColumn {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            return ResponseEntity.ok(kanbanElementService.addElement(request, principal.getName()));
        }
    }

    @Operation(summary = "Поиск элемента", description = "Доступен поиск по названию и тегу элемента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Указанного канбана не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к ресурсу"),
            @ApiResponse(responseCode = "200", description = "Список найденных элементов", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanElementMainDataResponse[].class))
            })
    })
    @GetMapping("/find")
    public KanbanElementMainDataResponse[] findElementsByName(@RequestParam @Parameter(description = "Идентиикатор канбана") long id,
                                                              @RequestParam SearchElementType type,
                                                              @RequestParam ElementStatus status,
                                                              @RequestParam String name,
                                                              @RequestParam int pageIndex, @RequestParam int rowCount,
                                                              Principal principal)
            throws ForbiddenException, NoSuchKanbanException {
        return kanbanElementService.findElements(id, type, name, status, principal.getName()).stream()
                .skip(pageIndex)
                .limit(rowCount)
                .toArray(KanbanElementMainDataResponse[]::new);
    }

    @Operation(summary = "Переименование элемента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Попытка обращения к несуществующему элементу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к ресурсу"),
            @ApiResponse(responseCode = "400", description = "Попытка присвоения элементу пустого имени", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Элемент успешно изменён"),
            @ApiResponse(responseCode = "410",
                    description = "Операция недоступна, поскольку элемент перемещён в корзину", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PutMapping("/rename")
    public ResponseEntity<?> rename(@RequestParam long id, @RequestBody @Valid NameRequest request,
                                    BindingResult bindingResult, Principal principal)
            throws ForbiddenException, IncorrectElementStatusException, NoSuchKanbanElementException {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            kanbanElementService.rename(id, request.getName(), principal.getName());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @Operation(summary = "Изменение даты, прикреплённой к элементу")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Попытка обращения к несуществующему элементу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к ресурсу"),
            @ApiResponse(responseCode = "400", description = "Неверный формат даты", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Элемент успешно изменён"),
            @ApiResponse(responseCode = "410",
                    description = "Операция недоступна, поскольку элемент перемещён в корзину", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PutMapping("/date")
    public void editDate(@RequestParam long id, @RequestParam String date, Principal principal)
            throws ForbiddenException, IncorrectElementStatusException, NoSuchKanbanElementException {
        kanbanElementService.editDate(id, date, principal.getName());
    }

    @Operation(summary = "Удаление даты, прикреплённой к элементу")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Попытка обращения к несуществующему элементу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к ресурсу"),
            @ApiResponse(responseCode = "200", description = "Элемент успешно изменён"),
            @ApiResponse(responseCode = "410",
                    description = "Операция недоступна, поскольку элемент перемещён в корзину", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @DeleteMapping("/date")
    public void dropDate(@RequestParam long id, Principal principal)
            throws ForbiddenException, IncorrectElementStatusException, NoSuchKanbanElementException {
        kanbanElementService.dropDate(id, principal.getName());
    }

    @Operation(summary = "Изменение контента элемента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Попытка обращения к несуществующему элементу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к ресурсу"),
            @ApiResponse(responseCode = "200", description = "Элемент успешно изменён"),
            @ApiResponse(responseCode = "410",
                    description = "Операция недоступна, поскольку элемент перемещён в корзину", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PutMapping("/content")
    public void editContent(@RequestParam long id, @RequestParam String content, Principal principal)
            throws ForbiddenException, IncorrectElementStatusException, NoSuchKanbanElementException {
        kanbanElementService.editContent(id, content, principal.getName());
    }

    @Operation(summary = "Удаление элемента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему элементу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к ресурсу"),
            @ApiResponse(responseCode = "200", description = "Элемент успешно удалён"),
            @ApiResponse(responseCode = "410",
                    description = "Операция недоступна, поскольку элемент уже в корзине", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @DeleteMapping()
    public void removeElement(@RequestParam long id, Principal principal)
            throws ForbiddenException, IncorrectElementStatusException, NoSuchKanbanElementException {
        kanbanElementService.utilizeElementFromUser(id, principal.getName());
    }

    @Operation(summary = "Перемещение элемента канбана",
            description = "Изменение местоположения элемента на указанный порядковый номер в указанном столбце")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Неверные идентификаторы колонки или элемента", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "400", description = "Введённый желаемый порядковый номер элемента недопостим",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Указанный элемент успешно перенесён"),
            @ApiResponse(responseCode = "410",
                    description = "Операция недоступна, поскольку элемент перемещён в корзину или архив", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PutMapping("/transport")
    public ResponseEntity<?> transportElement(@RequestBody @Valid TransportElementRequest transportElementRequest,
                                              BindingResult bindingResult, Principal principal)
            throws ForbiddenException, IncorrectElementStatusException, NoSuchKanbanElementException {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            if (kanbanElementService.transportElement(transportElementRequest, principal.getName())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ErrorResponse(Errors.INDEX_MORE_COLLECTION_SIZE),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Operation(summary = "Добавление существующего в этом канбане тега к указанному элементу")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему элементу или тегу",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "410", description = "Элемент перемещён в корзину и недоступен для изменеия",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Тег успешно добавлен"),
    })
    @PostMapping("/tag")
    public void addTag(@RequestParam long elementId, @RequestParam long tagId, Principal principal)
            throws NoSuchTagException, ForbiddenException, IncorrectElementStatusException, NoSuchKanbanElementException {
        attributesService.addTag(elementId, tagId, principal.getName());
    }

    @Operation(summary = "Удаление тега из элемента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему элементу",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "410", description = "Элемент перемещён в корзину и недоступен для изменеия",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Тег успешно удалён"),
    })
    @DeleteMapping("/tag")
    public void removeTag(@RequestParam long elementId, @RequestParam long tagId, Principal principal)
            throws IncorrectElementStatusException, NoSuchKanbanElementException, ForbiddenException {
        attributesService.removeTag(elementId, tagId, principal.getName());
    }
}
