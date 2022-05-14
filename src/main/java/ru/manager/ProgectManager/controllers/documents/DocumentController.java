package ru.manager.ProgectManager.controllers.documents;

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
import ru.manager.ProgectManager.DTO.request.ContentRequest;
import ru.manager.ProgectManager.DTO.request.GetResourceWithPagination;
import ru.manager.ProgectManager.DTO.request.NameRequest;
import ru.manager.ProgectManager.DTO.request.documents.CreatePageRequest;
import ru.manager.ProgectManager.DTO.request.documents.TransportPageRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.DTO.response.documents.*;
import ru.manager.ProgectManager.components.ErrorResponseEntityConfigurator;
import ru.manager.ProgectManager.components.authorization.JwtProvider;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.documents.PageService;
import ru.manager.ProgectManager.services.user.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/users/documents")
@RequiredArgsConstructor
@Tag(name = "Манипуляция страницами документации")
public class DocumentController {
    private final PageService pageService;
    private final JwtProvider provider;
    private final ErrorResponseEntityConfigurator entityConfigurator;
    private final UserService userService;

    @Operation(summary = "Добавление страницы документации")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Идентификатор созданного документа", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PostMapping("/add")
    public ResponseEntity<?> addPage(@RequestBody @Valid CreatePageRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                Optional<Long> id = pageService.createPage(request, provider.getLoginFromToken());
                if (id.isPresent()) {
                    return ResponseEntity.ok(new IdResponse(id.get()));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Переименование страницы документации")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Переименование прошло успешно"),
            @ApiResponse(responseCode = "400", description = "Название должно содержать видимые символы", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанной страницы не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PutMapping("/rename")
    public ResponseEntity<?> rename(@RequestParam @Parameter(description = "Идентификатор страницы") long id,
                                    @RequestBody @Valid NameRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (pageService.rename(id, request.getName(), provider.getLoginFromToken())) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PAGE), HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Изменение данных документа")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные успешно изменены"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанной страницы не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PutMapping("/content")
    public ResponseEntity<?> setContent(@RequestBody ContentRequest request) {
        try {
            if (pageService.setContent(request.getId(), request.getContent(), provider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PAGE), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Получение содержимого страницы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Содержимое запрашиваемой страницы", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageContentResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанной страницы не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/content")
    public ResponseEntity<?> getContent(@RequestParam @Parameter(description = "Идентификатор страницы") long id) {
        try {
            String login = provider.getLoginFromToken();
            int zoneId = userService.findZoneIdForThisUser(login);
            Optional<PageContentResponse> response = pageService.findContent(id, login, zoneId);
            if (response.isPresent()) {
                return ResponseEntity.ok(response.get());
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PAGE), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Публикация документа",
            description = "До публикации доступ к документу имеет только его создатель")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Документ успешно опубликован"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанной страницы не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PutMapping("/publish")
    public ResponseEntity<?> publish(@RequestParam @Parameter(description = "Идентификатор страницы") long id) {
        try {
            if (pageService.publish(id, provider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PAGE), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Удаление страницы документа")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Страница успешно удалена"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанной страницы не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam @Parameter(description = "Идентификатор страницы") long id) {
        try {
            if (pageService.deletePage(id, provider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PAGE), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Получение страницы документа по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запрашиваемая страница", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанной страницы не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/get")
    public ResponseEntity<?> findById(@RequestParam @Parameter(description = "Идентификатор страницы") long id) {
        try {
            Optional<PageResponse> page = pageService.find(id, provider.getLoginFromToken());
            if (page.isPresent()) {
                return ResponseEntity.ok(page.get());
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PAGE), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Получение списка корневых страниц документов проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список корневых страниц документов", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageResponseList.class))
            }),
            @ApiResponse(responseCode = "400", description = "Некорректные индексы пагинации", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/root")
    public ResponseEntity<?> findRootPages(@RequestBody @Valid GetResourceWithPagination request,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            try {
                Optional<List<PageResponse>> pageList = pageService.findAllRoot(request.getId(),
                        provider.getLoginFromToken());
                if (pageList.isPresent()) {
                    return ResponseEntity.ok(new PageResponseList(pageList.get(), request.getPageIndex(),
                            request.getCount()));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Поиск страниц по названию среди документов указанного проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список найденных страниц документов", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageNameResponseList.class))
            }),
            @ApiResponse(responseCode = "400", description = "Некорректные индексы пагинации", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/find")
    public ResponseEntity<?> findByName(@RequestBody @Valid GetResourceWithPagination request,
                                        BindingResult bindingResult,
                                        @RequestParam String name) {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            try {
                Optional<Set<PageNameResponse>> pageSet = pageService.findByName(request.getId(), name,
                        provider.getLoginFromToken());
                if (pageSet.isPresent()) {
                    return ResponseEntity.ok(new PageNameResponseList(pageSet.get(), request.getPageIndex(),
                            request.getCount()));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Список всех страниц документов, отсортированный по дате последнего изменения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список страниц документов", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageNameAndUpdateDateResponseList.class))
            }),
            @ApiResponse(responseCode = "400", description = "Некорректные индексы пагинации", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/all")
    public ResponseEntity<?> findAllWithSort(@RequestBody @Valid GetResourceWithPagination request,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            try {
                String login = provider.getLoginFromToken();
                int zoneId = userService.findZoneIdForThisUser(login);
                Optional<List<PageNameAndUpdateDateResponse>> pages = pageService.findAllWithSort(request.getId(),
                        login, zoneId);
                if (pages.isPresent()) {
                    return ResponseEntity.ok(new PageNameAndUpdateDateResponseList(pages.get(), request.getPageIndex(),
                            request.getCount()));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Список последних посещённых пользователем страниц документов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список страниц документов", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageNameAndUpdateDateResponseList.class))
            }),
            @ApiResponse(responseCode = "400", description = "Некорректные индексы пагинации", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/lasts")
    public ResponseEntity<?> findLastSee(@RequestBody @Valid GetResourceWithPagination request,
                                         BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            String login = provider.getLoginFromToken();
            int zoneId = userService.findZoneIdForThisUser(login);
            Optional<List<PageNameAndUpdateDateResponse>> responses = pageService.findLastSeeDocument(request.getId(),
                    login, zoneId);
            if (responses.isPresent()) {
                return ResponseEntity.ok(new PageNameAndUpdateDateResponseList(responses.get(), request.getPageIndex(),
                        request.getCount()));
            } else {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Перемещение страницы документа")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перемещение произошло успешно"),
            @ApiResponse(responseCode = "400", description = "Некорректный целевой индекс", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанной страницы не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PutMapping("/transport")
    public ResponseEntity<?> transport(@RequestBody @Valid TransportPageRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.INDEX_MUST_BE_MORE_0), HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (pageService.transport(request, provider.getLoginFromToken())) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PAGE), HttpStatus.NOT_FOUND);
            }
        }
    }
}