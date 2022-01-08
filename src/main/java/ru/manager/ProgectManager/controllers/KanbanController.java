package ru.manager.ProgectManager.controllers;

import io.swagger.v3.oas.annotations.Operation;
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
import ru.manager.ProgectManager.DTO.response.ContentDTO;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.KanbanResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.components.PhotoCompressor;
import ru.manager.ProgectManager.entitys.KanbanColumn;
import ru.manager.ProgectManager.entitys.KanbanElement;
import ru.manager.ProgectManager.services.KanbanService;
import ru.manager.ProgectManager.services.ProjectService;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/kanban")
@Tag(name = "Контроллер получения и изменения канбан-доски")
public class KanbanController {
    private final ProjectService projectService;
    private final KanbanService kanbanService;
    private final JwtProvider provider;
    private final PhotoCompressor compressor;

    @Operation(summary = "Получение канбан-доски",
            description = "Получение всего канбана, кроме контента элементов колонок")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Указанного проекта не сущесвует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к указанному проекту",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanResponse.class))
            })
    })
    @GetMapping("/get")
    public ResponseEntity<?> getKanban(@RequestParam long projectId) {
        try {
            Optional<List<KanbanColumn>> result = projectService.findKanbans(projectId, provider.getLoginFromToken());
            if (result.isPresent()) {
                return ResponseEntity.ok(new KanbanResponse(result.get()));
            } else {
                return new ResponseEntity<>(
                        new ErrorResponse(Collections.singletonList("User: The user does not have access to this project")),
                        HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Collections.singletonList("Project: No such specified project")),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Полвучние элемента канбана", description = "Получегие полной информации об элементе канбана")
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
                            schema = @Schema(implementation = ContentDTO.class))
            })
    })
    @GetMapping("/element")
    public ResponseEntity<?> getContent(@RequestParam long elementId) {
        try {
            Optional<KanbanElement> content = kanbanService
                    .getContentFromElement(elementId, provider.getLoginFromToken());
            if (content.isPresent()) {
                return ResponseEntity.ok(new ContentDTO(content.get()));
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Collections.singletonList("Element: No such specified element")),
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
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                if (kanbanService.transportElement(transportElementRequest, provider.getLoginFromToken())) {
                    return ResponseEntity.ok(new KanbanResponse(projectService
                            .findKanbans(kanbanService.findProjectFromElement(transportElementRequest.getId()).getId(),
                                    provider.getLoginFromToken()).get()));
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(
                        new ErrorResponse(Collections.singletonList("Request: No such specified element or column")),
                        HttpStatus.BAD_REQUEST);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(new ErrorResponse(Collections.singletonList(e.getMessage())),
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
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            String login = provider.getLoginFromToken();
            try {
                if (kanbanService.transportColumn(transportColumnRequest, login)) {
                    return ResponseEntity.ok(new KanbanResponse(projectService
                            .findKanbans(kanbanService
                                    .findProjectFromColumn(transportColumnRequest.getId()).getId(), login).get()));
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Collections.singletonList("Column: No such specified column")),
                        HttpStatus.BAD_REQUEST);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(new ErrorResponse(Collections.singletonList(e.getMessage())),
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
            @ApiResponse(responseCode = "200", description = "Канбан доска с учётом внесённых изменений", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanResponse.class))
            })
    })
    @PostMapping("/element")
    public ResponseEntity<?> editElement(@RequestBody @Valid CreateKanbanElementRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                String login = provider.getLoginFromToken();
                if (kanbanService.addElement(request, login)) {
                    return ResponseEntity.ok(new KanbanResponse(projectService
                            .findKanbans(kanbanService.findProjectFromColumn(request.getColumnId()).getId(), login)
                            .get()));
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Collections.singletonList("Column: No such specified column")),
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
            @ApiResponse(responseCode = "200", description = "Канбан доска с учётом внесённых изменений", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanResponse.class))
            })
    })
    @PutMapping("/element")
    public ResponseEntity<?> addElement(@RequestParam long id, @RequestBody @Valid UpdateKanbanElementRequest request,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::toString)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                String login = provider.getLoginFromToken();
                if (kanbanService.setElement(id, request, login)) {
                    return ResponseEntity.ok(new KanbanResponse(projectService
                            .findKanbans(kanbanService.findProjectFromElement(id).getId(), login)
                            .get()));
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(
                        new ErrorResponse(Collections.singletonList("Request: No such specified element")),
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Operation(summary = "Добавление колонки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующему элементу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "406", description = "Название колонки не должно быть пустым", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Канбан доска с учётом внесённых изменений", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanResponse.class))
            })
    })
    @PostMapping("/column")
    public ResponseEntity<?> addColumn(@RequestBody @Valid KanbanColumnRequest kanbanColumnRequest,
                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::toString)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            String login = provider.getLoginFromToken();
            if (projectService.addColumn(kanbanColumnRequest, login))
                return ResponseEntity.ok(new KanbanResponse(projectService
                        .findKanbans(kanbanColumnRequest.getProjectId(), login).get()));
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Collections.singletonList("Project: No such specified project")),
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
            @ApiResponse(responseCode = "406", description = "Название колонки не должно быть пустым", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Канбан доска с учётом внесённых изменений", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanResponse.class))
            })
    })
    @PutMapping("/column")
    public ResponseEntity<?> renameColumn(@RequestParam long id, @RequestBody @Valid NameRequest name,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::toString)
                            .collect(Collectors.toList())),
                    HttpStatus.NOT_ACCEPTABLE);
        }
        try {
            String login = provider.getLoginFromToken();
            if (kanbanService.renameColumn(id, name.getName(), login))
                return ResponseEntity.ok(new KanbanResponse(projectService
                        .findKanbans(kanbanService.findProjectFromColumn(id).getId(), login)
                        .get()));
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Collections.singletonList("Column: No such specified column")),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Добавление картинки", description = "Добавление или изменение картинки элемента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующему элементу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "200", description = "Картинка успешно сжата и добавлена или изменена")
    })
    @PostMapping("/photo")
    public ResponseEntity<?> addPhoto(@RequestParam long id, @ModelAttribute PhotoDTO photoDTO) {
        try {
            if (kanbanService.setPhoto(id, provider.getLoginFromToken(), compressor.compress(photoDTO.getFile()))) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Collections.singletonList("Element: No such specified element")),
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
            @ApiResponse(responseCode = "200", description = "Канбан доска с учётом внесённых изменений", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanResponse.class))
            })
    })
    @DeleteMapping("/element")
    public ResponseEntity<?> removeElement(@RequestParam long id) {
        try {
            String login = provider.getLoginFromToken();
            if (kanbanService.deleteElement(id, login)) {
                return ResponseEntity.ok(new KanbanResponse(projectService
                        .findKanbans(kanbanService.findProjectFromElement(id).getId(), login).get()));
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Collections.singletonList("Element: No such specified element")),
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
            @ApiResponse(responseCode = "200", description = "Канбан доска с учётом внесённых изменений", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanResponse.class))
            })
    })
    @DeleteMapping("/column")
    public ResponseEntity<?> removeColumn(@RequestParam long id) {
        try {
            String login = provider.getLoginFromToken();
            if (kanbanService.deleteColumn(id, login)) {
                return ResponseEntity.ok(new KanbanResponse(projectService
                        .findKanbans(kanbanService.findProjectFromColumn(id).getId(), login).get()));
            }
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Collections.singletonList("Column: No such specified column")),
                    HttpStatus.BAD_REQUEST);
        }
    }
}
