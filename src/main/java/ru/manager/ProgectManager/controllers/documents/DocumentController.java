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
import ru.manager.ProgectManager.DTO.request.NameRequest;
import ru.manager.ProgectManager.DTO.request.documents.CreatePageRequest;
import ru.manager.ProgectManager.DTO.request.documents.TransportPageRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.DTO.response.documents.*;
import ru.manager.ProgectManager.entitys.documents.Page;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.documents.PageService;
import ru.manager.ProgectManager.services.user.UserService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/users/documents")
@RequiredArgsConstructor
@Tag(name = "Манипуляция страницами документации")
public class DocumentController {
    private final PageService pageService;
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
    @PostMapping
    public ResponseEntity<?> addPage(@RequestBody @Valid CreatePageRequest request, BindingResult bindingResult,
                                     Principal principal) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                Optional<Long> id = pageService.createPage(request, principal.getName());
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
                                    @RequestBody @Valid NameRequest request, BindingResult bindingResult,
                                    Principal principal) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (pageService.rename(id, request.getName(), principal.getName())) {
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
    public ResponseEntity<?> setContent(@RequestBody ContentRequest request, Principal principal) {
        try {
            if (pageService.setContent(request.getId(), request.getContent(), principal.getName())) {
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
    public ResponseEntity<?> getContent(@RequestParam @Parameter(description = "Идентификатор страницы") long id,
                                        Principal principal) {
        try {
            String login = principal.getName();
            Optional<Page> response = pageService.find(id, login);
            if (response.isPresent()) {
                return ResponseEntity.ok(new PageContentResponse(response.get(),
                        userService.findZoneIdForThisUser(login), pageService.canEditPage(response.get(), login)));
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
    public ResponseEntity<?> publish(@RequestParam @Parameter(description = "Идентификатор страницы") long id,
                                     Principal principal) {
        try {
            if (pageService.publish(id, principal.getName())) {
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
    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam @Parameter(description = "Идентификатор страницы") long id,
                                    Principal principal) {
        try {
            if (pageService.deletePage(id, principal.getName())) {
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
                            schema = @Schema(implementation = PageNameResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанной страницы не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping
    public ResponseEntity<?> findById(@RequestParam @Parameter(description = "Идентификатор страницы") long id,
                                      Principal principal) {
        try {
            String login = principal.getName();
            Optional<Page> page = pageService.find(id, login);
            if (page.isPresent()) {
                return ResponseEntity.ok(new PageNameResponse(page.get(), pageService.canEditPage(page.get(), login)));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PAGE), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Получение полной информации о странице документа по идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Запрашиваемая страница", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageAllDataResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанной страницы не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/data")
    public ResponseEntity<?> findAllPageData(@RequestParam @Parameter(description = "Идентификатор страницы") long id,
                                             Principal principal) {
        try {
            String login = principal.getName();
            Optional<Page> page = pageService.find(id, login);
            if (page.isPresent()) {
                return ResponseEntity.ok(new PageAllDataResponse(page.get(), userService.findZoneIdForThisUser(login),
                        pageService.canEditPage(page.get(), login)));
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
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/root")
    public ResponseEntity<?> findRootPages(@RequestParam @Parameter(description = "Идентификатор проекта") long id,
                                           @RequestParam int pageIndex, @RequestParam int rowCount,
                                           Principal principal) {
        try {
            Optional<List<PageResponse>> pageList = pageService.findAllRoot(id, principal.getName());
            if (pageList.isPresent()) {
                return ResponseEntity.ok(new PageResponseList(pageList.get(), pageIndex, rowCount));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Поиск страниц по названию среди документов указанного проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список найденных страниц документов", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageNameAndUpdateDateResponseList.class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/find")
    public ResponseEntity<?> findByName(@RequestParam @Parameter(description = "Идентификатор проекта") long id,
                                        @RequestParam int pageIndex, @RequestParam int rowCount,
                                        @RequestParam String name, Principal principal) {
        try {
            String login = principal.getName();
            Optional<List<PageNameAndUpdateDateResponse>> pageSet = pageService.findByName(id, name, login,
                    userService.findZoneIdForThisUser(login));
            if (pageSet.isPresent()) {
                return ResponseEntity.ok(new PageNameAndUpdateDateResponseList(pageSet.get(), pageIndex, rowCount));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Список всех страниц документов, отсортированный по дате последнего изменения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список страниц документов", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageNameAndUpdateDateResponseList.class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/all")
    public ResponseEntity<?> findAllWithSort(@RequestParam @Parameter(description = "Идентификатор проекта") long id,
                                             @RequestParam int pageIndex, @RequestParam int rowCount,
                                             Principal principal) {
        try {
            String login = principal.getName();
            int zoneId = userService.findZoneIdForThisUser(login);
            Optional<List<PageNameAndUpdateDateResponse>> pages = pageService.findAllWithSort(id, login, zoneId);
            if (pages.isPresent()) {
                return ResponseEntity.ok(new PageNameAndUpdateDateResponseList(pages.get(), pageIndex, rowCount));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Список последних посещённых пользователем страниц документов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список страниц документов", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageNameAndUpdateDateResponseList.class))
            }),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/lasts")
    public ResponseEntity<?> findLastSee(@RequestParam @Parameter(description = "Идентификатор проекта") long id,
                                         @RequestParam int pageIndex, @RequestParam int rowCount, Principal principal) {
        String login = principal.getName();
        int zoneId = userService.findZoneIdForThisUser(login);
        Optional<List<PageNameAndUpdateDateResponse>> responses = pageService.findLastSeeDocument(id, login, zoneId);
        if (responses.isPresent()) {
            return ResponseEntity.ok(new PageNameAndUpdateDateResponseList(responses.get(), pageIndex, rowCount));
        } else {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Список подстраниц указанной страницы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список постраниц", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageResponseList.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/children")
    public ResponseEntity<?> findChildren(@RequestParam long id, @RequestParam int pageIndex, @RequestParam int rowCount,
                                          Principal principal) {
        try {
            Optional<List<PageResponse>> pages = pageService.findSubpages(id, principal.getName());
            if(pages.isPresent()) {
                return ResponseEntity.ok(new PageResponseList(pages.get(), pageIndex, rowCount));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PAGE), HttpStatus.NOT_FOUND);
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
    public ResponseEntity<?> transport(@RequestBody @Valid TransportPageRequest request, BindingResult bindingResult,
                                       Principal principal) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.INDEX_MUST_BE_MORE_0), HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (pageService.transport(request, principal.getName())) {
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