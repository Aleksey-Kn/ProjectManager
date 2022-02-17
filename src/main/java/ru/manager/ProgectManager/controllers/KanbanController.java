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
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.*;
import ru.manager.ProgectManager.DTO.response.*;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.*;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.KanbanService;
import ru.manager.ProgectManager.services.ProjectService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/kanban")
@Tag(name = "Получение и изменения канбан-доски")
public class KanbanController {
    private final ProjectService projectService;
    private final KanbanService kanbanService;
    private final JwtProvider provider;

    @Operation(summary = "Добавление новой канбан-доски в проект")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Указанного проекта не сущесвует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к указанному проекту"),
            @ApiResponse(responseCode = "406", description = "Имя не должно быть пустым",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Kanban.class))
            })
    })
    @PostMapping("/new")
    public ResponseEntity<?> createKanban(@RequestParam long projectId, @RequestBody @Valid NameRequest name,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                Optional<Kanban> kanban =
                        projectService.createKanban(projectId, name.getName(), provider.getLoginFromToken());
                if (kanban.isPresent()) {
                    return ResponseEntity.ok(kanban.get());
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Operation(summary = "Получение канбан-доски",
            description = "Получение всего канбана, кроме контента элементов колонок")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Указанного канбана не сущесвует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "406", description = "Указаны некорректные индекс или количество элементов",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к указанному проекту"),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanResponse.class))
            })
    })
    @GetMapping("/get")
    public ResponseEntity<?> getKanban(@RequestBody @Valid GetKanbanRequest kanbanRequest, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                Optional<Kanban> result = kanbanService.findKanban(kanbanRequest.getId(), provider.getLoginFromToken());
                if (result.isPresent()) {
                    return ResponseEntity.ok(new KanbanResponse(result.get(),
                            kanbanRequest.getPageColumnIndex(), kanbanRequest.getCountColumn(),
                            kanbanRequest.getPageElementIndex(), kanbanRequest.getCountElement()));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Operation(summary = "Получение элемента канбана", description = "Получение полной информации об элементе канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Указанного элемента не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Запрашивающий пользователь не имеет доступа к проекту",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanElementContentResponse.class))
            })
    })
    @GetMapping("/element")
    public ResponseEntity<?> getContent(@RequestParam long elementId,
                                        @RequestParam @Parameter(description = "Часовой пояс текущего пользователя") int zoneId) {
        try {
            Optional<KanbanElement> content = kanbanService
                    .getContentFromElement(elementId, provider.getLoginFromToken());
            if (content.isPresent()) {
                return ResponseEntity.ok(new KanbanElementContentResponse(content.get(), zoneId));
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Перемещение элемента канбана",
            description = "Изменение местоположения элемента на указанный порядковый номер в указанном столбце")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Неверные идентификаторы колонки или элемента", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному проекту"),
            @ApiResponse(responseCode = "406", description = "Введённый желаемый порядковый номер элемента недопостим",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Канбан доска с учётом внесённых изменений", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanResponse.class))
            })
    })
    @PutMapping("/transport_element")
    public ResponseEntity<?> transportElement(@RequestBody @Valid TransportElementRequest transportElementRequest,
                                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                if (kanbanService.transportElement(transportElementRequest, provider.getLoginFromToken())) {
                    return ResponseEntity.ok(
                            new KanbanResponse(kanbanService.findKanbanFromElement(transportElementRequest.getId()),
                                    transportElementRequest.getPageColumnIndex(),
                                    transportElementRequest.getCountColumn(),
                                    transportElementRequest.getPageElementIndex(),
                                    transportElementRequest.getCountElement()));
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(
                        new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT_OR_COLUMN),
                        HttpStatus.BAD_REQUEST);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.INDEX_MORE_COLLECTION_SIZE),
                        HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }

    @Operation(summary = "Перемещение столбца канбана",
            description = "Изменение местоположения столбца на указанный порядковый номер")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Неверный идентификатор колонки", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному проекту"),
            @ApiResponse(responseCode = "406", description = "Введённый желаемый порядковый номер колонки недопостим",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Канбан доска с учётом внесённых изменений", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanResponse.class))
            })
    })
    @PutMapping("/transport_column")
    public ResponseEntity<?> transportColumn(@RequestBody @Valid TransportColumnRequest transportColumnRequest,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            String login = provider.getLoginFromToken();
            try {
                if (kanbanService.transportColumn(transportColumnRequest, login)) {
                    return ResponseEntity.ok(
                            new KanbanResponse(kanbanService.findKanbanFromColumn(transportColumnRequest.getId()),
                                    transportColumnRequest.getPageColumnIndex(), transportColumnRequest.getCountColumn(),
                                    transportColumnRequest.getPageElementIndex(), transportColumnRequest.getCountElement()));
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COLUMN),
                        HttpStatus.BAD_REQUEST);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.INDEX_MORE_COLLECTION_SIZE),
                        HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }

    @Operation(summary = "Добавление элемента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Указанной колонки не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "406", description = "Попытка создать элемент с пустым именем", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Добавленный элемент", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanElementContentResponse.class))
            })
    })
    @PostMapping("/element")
    public ResponseEntity<?> addElement(@RequestBody @Valid CreateKanbanElementRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                String login = provider.getLoginFromToken();
                Optional<KanbanElement> element = kanbanService.addElement(request, login);
                if (element.isPresent()) {
                    return ResponseEntity.ok(new KanbanElementContentResponse(element.get(), 0));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COLUMN),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Operation(summary = "Изменение элемента", description = "Обновление всех текстовых полей элемента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Попытка обращения к несуществующему элементу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "406", description = "Попытка присвоения элементу пустого имени", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Елемент с учётом внесённых изменений", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanElementContentResponse.class))
            })
    })
    @PutMapping("/element")
    public ResponseEntity<?> editElement(@RequestParam long id,
                                         @RequestParam @Parameter(description = "Часовой пояс текущего пользователя") int zoneId,
                                         @RequestBody @Valid UpdateKanbanElementRequest request,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                String login = provider.getLoginFromToken();
                Optional<KanbanElement> element = kanbanService.setElement(id, request, login);
                if (element.isPresent()) {
                    return ResponseEntity.ok(new KanbanElementContentResponse(element.get(), zoneId));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(
                        new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Operation(summary = "Добавление колонки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующему проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "406", description = "Название колонки не должно быть пустым", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Добавленная колонка", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanColumnResponse.class))
            })
    })
    @PostMapping("/column")
    public ResponseEntity<?> addColumn(@RequestBody @Valid KanbanColumnRequest kanbanColumnRequest,
                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            String login = provider.getLoginFromToken();
            Optional<KanbanColumn> column = kanbanService.addColumn(kanbanColumnRequest, login);
            if (column.isPresent()) {
                return ResponseEntity.ok(new KanbanColumnResponse(column.get(), 0, 1));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Изменение колонки", description = "Изменение названия колонки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующей колонке", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "406", description = "Переданные данные неприемлемы", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Колонка с учётом внесённых изменений", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanColumnResponse.class))
            })
    })
    @PutMapping("/column")
    public ResponseEntity<?> renameColumn(@RequestParam long id, @RequestParam int pageIndex, @RequestParam int rowCount,
                                          @RequestBody @Valid NameRequest name, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            if(pageIndex < 0) {
                return new ResponseEntity<>(new ErrorResponse(Errors.INDEX_MUST_BE_MORE_0), HttpStatus.NOT_ACCEPTABLE);
            }
            if(rowCount < 1) {
                return new ResponseEntity<>(new ErrorResponse(Errors.COUNT_MUST_BE_MORE_1), HttpStatus.NOT_ACCEPTABLE);
            }
            try {
                String login = provider.getLoginFromToken();
                Optional<KanbanColumn> column = kanbanService.renameColumn(id, name.getName(), login);
                if (column.isPresent()) {
                    return ResponseEntity.ok(new KanbanColumnResponse(column.get(), pageIndex, rowCount));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COLUMN),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Operation(summary = "Добавление вложения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующему элементу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "200", description = "Элемент с учётом добавленного вложения", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanElementContentResponse.class))
            }),
            @ApiResponse(responseCode = "406", description = "Ошибка чтения файла", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PostMapping("/attachment")
    public ResponseEntity<?> addAttachment(@RequestParam @Parameter(description = "Часовой пояс текущего пользователя") int zoneId,
                                           @RequestParam long id, @ModelAttribute PhotoDTO photoDTO) {
        try {
            Optional<KanbanElement> element =
                    kanbanService.addAttachment(id, provider.getLoginFromToken(), photoDTO.getFile());
            if (element.isPresent()) {
                return ResponseEntity.ok(new KanbanElementContentResponse(element.get(), zoneId));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT),
                    HttpStatus.BAD_REQUEST);
        } catch (IOException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.BAD_FILE), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @Operation(summary = "Получение вложения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующему вложению", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "200", description = "Запрашиваемое вложение", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanAttachment.class))
            })
    })
    @GetMapping("/attachment")
    public ResponseEntity<?> getAttachment(@RequestParam long id){
        try{
            Optional<KanbanAttachment> attachment = kanbanService.getAttachment(id, provider.getLoginFromToken());
            if(attachment.isPresent()){
                return ResponseEntity.ok(attachment.get());
            } else{
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ATTACHMENT), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Удаление всей канбан-доски")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующему канбану", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "200", description = "Канбан доска успешно удалена")
    })
    @DeleteMapping("/all_kanban")
    public ResponseEntity<?> removeKanban(@RequestParam long id) {
        try {
            if (projectService.removeKanban(id, provider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Удаление элемента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующему элементу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "406", description = "Указаны некорректные индекс или количество элементов",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200",
                    description = "Колонка, в которой находился удалённый элемент, с учётом внесённых изменений",
                    content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanResponse.class))
            })
    })
    @DeleteMapping("/element")
    public ResponseEntity<?> removeElement(@RequestParam long id, @RequestParam int pageIndex, @RequestParam int rowCount) {
        if(pageIndex < 0) {
            return new ResponseEntity<>(new ErrorResponse(Errors.INDEX_MUST_BE_MORE_0), HttpStatus.NOT_ACCEPTABLE);
        }
        if(rowCount < 1) {
            return new ResponseEntity<>(new ErrorResponse(Errors.COUNT_MUST_BE_MORE_1), HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            String login = provider.getLoginFromToken();
            Optional<KanbanColumn> column = kanbanService.deleteElement(id, login);
            if (column.isPresent()) {
                return ResponseEntity.ok(new KanbanColumnResponse(column.get(), pageIndex, rowCount));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Удаление колонки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующей колонке", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "406", description = "Указаны некорректные индекс или количество элементов",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Канбан доска с учётом внесённых изменений", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanResponse.class))
            })
    })
    @DeleteMapping("/column")
    public ResponseEntity<?> removeColumn(@RequestBody @Valid GetKanbanRequest request, BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                String login = provider.getLoginFromToken();
                if (kanbanService.deleteColumn(request.getId(), login)) {
                    return ResponseEntity.ok(new KanbanResponse(kanbanService.findKanbanFromColumn(request.getId()),
                            request.getPageColumnIndex(), request.getCountColumn(),
                            request.getPageElementIndex(), request.getCountElement()));
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COLUMN),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Operation(summary = "Добавление комментария к элементу канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующему элементу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "406", description = "Неподходящие текстовые данные",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Полная информация о добавленном комментарии", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanElementCommentResponse.class))
            })
    })
    @PostMapping("/comment")
    public ResponseEntity<?> addComment(@RequestBody @Valid KanbanCommentRequest request, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                Optional<KanbanElementComment> comment = kanbanService.addComment(request, provider.getLoginFromToken());
                if (comment.isPresent()) {
                    return ResponseEntity.ok(new KanbanElementCommentResponse(comment.get(), request.getZone()));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e){
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Operation(summary = "Удаление комментария элемента канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующему комментарию", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному комментарию"),
            @ApiResponse(responseCode = "200", description = "Элемент канбана с учётом внесённых изменений", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanElementContentResponse.class))
            })
    })
    @DeleteMapping("/comment")
    public ResponseEntity<?> removeComment(@RequestParam long id,
                                           @RequestParam @Parameter(description = "Часовой пояс текущего пользователя") int zoneId){
        try{
            Optional<KanbanElement> element = kanbanService.removeComment(id, provider.getLoginFromToken());
            if(element.isPresent()){
                return ResponseEntity.ok(new KanbanElementContentResponse(element.get(), zoneId));
            } else{
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COMMENT), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Изменение комментария элемента канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующему элементу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "406", description = "Неподходящие текстовые данные",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Полная информация о изменённом комментарии", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanElementCommentResponse.class))
            })
    })
    @PutMapping("/comment")
    public ResponseEntity<?> updateComment(@RequestBody @Valid KanbanCommentRequest request, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                Optional<KanbanElementComment> comment = kanbanService.updateComment(request, provider.getLoginFromToken());
                if (comment.isPresent()) {
                    return ResponseEntity.ok(new KanbanElementCommentResponse(comment.get(), request.getZone()));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COMMENT), HttpStatus.BAD_REQUEST);
            }
        }
    }
}
