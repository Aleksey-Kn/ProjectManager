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
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.documents.NoSuchPageException;
import ru.manager.ProgectManager.exception.project.NoSuchProjectException;
import ru.manager.ProgectManager.services.documents.PageService;
import ru.manager.ProgectManager.services.user.UserService;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/users/documents")
@RequiredArgsConstructor
@Tag(name = "Манипуляция страницами документации")
public class DocumentController {
    private final PageService pageService;
    private final UserService userService;

    @ExceptionHandler(NoSuchPageException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse noSuchPageExceptionHandler() {
        return new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PAGE);
    }

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
                                     Principal principal) throws ForbiddenException, NoSuchProjectException {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            return ResponseEntity.ok(new IdResponse(pageService.createPage(request, principal.getName())));
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
                                    Principal principal) throws ForbiddenException, NoSuchProjectException {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            pageService.rename(id, request.getName(), principal.getName());
            return new ResponseEntity<>(HttpStatus.OK);
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
    public void setContent(@RequestBody ContentRequest request, Principal principal)
            throws ForbiddenException, NoSuchPageException {
        pageService.setContent(request.getId(), request.getContent(), principal.getName());
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
    public PageContentResponse getContent(@RequestParam @Parameter(description = "Идентификатор страницы") long id,
                                          Principal principal) throws ForbiddenException, NoSuchPageException {
        return pageService.findContent(id, principal.getName());
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
    public void publish(@RequestParam @Parameter(description = "Идентификатор страницы") long id,
                        Principal principal) throws ForbiddenException, NoSuchPageException {
        pageService.publish(id, principal.getName());
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
    public void delete(@RequestParam @Parameter(description = "Идентификатор страницы") long id,
                       Principal principal) throws ForbiddenException, NoSuchPageException {
        pageService.deletePage(id, principal.getName());
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
    public PageNameResponse findById(@RequestParam @Parameter(description = "Идентификатор страницы") long id,
                                     Principal principal) throws ForbiddenException, NoSuchPageException {
        return pageService.findName(id, principal.getName());
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
    public PageAllDataResponse findAllPageData(@RequestParam @Parameter(description = "Идентификатор страницы") long id,
                                               Principal principal) throws ForbiddenException, NoSuchPageException {
        return pageService.findAllData(id, principal.getName());
    }

    @Operation(summary = "Получение списка корневых страниц документов проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список корневых страниц документов", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse[].class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/root")
    public PageResponse[] findRootPages(@RequestParam @Parameter(description = "Идентификатор проекта") long id,
                                        @RequestParam int pageIndex, @RequestParam int rowCount,
                                        Principal principal) throws ForbiddenException, NoSuchProjectException {
        return pageService.findAllRoot(id, principal.getName()).stream().skip(pageIndex).limit(rowCount)
                .toArray(PageResponse[]::new);
    }

    @Operation(summary = "Поиск страниц по названию среди документов указанного проекта")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список найденных страниц документов", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageNameAndUpdateDateResponse[].class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/find")
    public PageNameAndUpdateDateResponse[] findByName(@RequestParam @Parameter(description = "Идентификатор проекта") long id,
                                                      @RequestParam int pageIndex, @RequestParam int rowCount,
                                                      @RequestParam String name, Principal principal) throws ForbiddenException, NoSuchProjectException {
        String login = principal.getName();
        return pageService.findByName(id, name, login, userService.findZoneIdForThisUser(login)).stream()
                .skip(pageIndex)
                .limit(rowCount)
                .toArray(PageNameAndUpdateDateResponse[]::new);
    }

    @Operation(summary = "Список всех страниц документов, отсортированный по дате последнего изменения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список страниц документов", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageNameAndUpdateDateResponse[].class))
            }),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав доступа для совершения данного действия"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/all")
    public PageNameAndUpdateDateResponse[] findAllWithSort(@RequestParam @Parameter(description = "Идентификатор проекта") long id,
                                                           @RequestParam int pageIndex, @RequestParam int rowCount,
                                                           Principal principal) throws ForbiddenException, NoSuchProjectException {
        String login = principal.getName();
        return pageService.findAllWithSort(id, login, userService.findZoneIdForThisUser(login)).stream()
                .skip(pageIndex)
                .limit(rowCount)
                .toArray(PageNameAndUpdateDateResponse[]::new);
    }

    @Operation(summary = "Список последних посещённых пользователем страниц документов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список страниц документов", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageNameAndUpdateDateResponse[].class))
            }),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/lasts")
    public PageNameAndUpdateDateResponse[] findLastSee(@RequestParam @Parameter(description = "Идентификатор проекта") long id,
                                                       @RequestParam int pageIndex, @RequestParam int rowCount,
                                                       Principal principal) throws NoSuchProjectException {
        String login = principal.getName();
        return pageService.findLastSeeDocument(id, login, userService.findZoneIdForThisUser(login)).stream()
                .skip(pageIndex)
                .limit(rowCount)
                .toArray(PageNameAndUpdateDateResponse[]::new);
    }

    @Operation(summary = "Список подстраниц указанной страницы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список подстраниц", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse[].class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/children")
    public PageResponse[] findChildren(@RequestParam long id, @RequestParam int pageIndex, @RequestParam int rowCount,
                                       Principal principal) throws ForbiddenException, NoSuchPageException {
        return pageService.findSubpages(id, principal.getName()).stream().skip(pageIndex).limit(rowCount)
                .toArray(PageResponse[]::new);
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
                                       Principal principal) throws ForbiddenException, NoSuchPageException {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.INDEX_MUST_BE_MORE_0), HttpStatus.BAD_REQUEST);
        } else {
            pageService.transport(request, principal.getName());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }
}