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
import ru.manager.ProgectManager.DTO.request.kanban.CreateKanbanElementRequest;
import ru.manager.ProgectManager.DTO.request.kanban.TransportElementRequest;
import ru.manager.ProgectManager.DTO.request.kanban.UpdateKanbanElementRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanElementContentResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.kanban.KanbanColumn;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.IncorrectStatusException;
import ru.manager.ProgectManager.services.kanban.KanbanElementService;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/kanban/element")
@Tag(name = "Манипуляции с элементами канбан-доски")
public class KanbanElementController {
    private final KanbanElementService kanbanElementService;
    private final JwtProvider provider;

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
    @GetMapping("/get")
    public ResponseEntity<?> getContent(@RequestParam long elementId,
                                        @RequestParam @Parameter(description = "Часовой пояс текущего пользователя") int zoneId) {
        try {
            Optional<KanbanElement> content = kanbanElementService
                    .getContentFromElement(elementId, provider.getLoginFromToken());
            if (content.isPresent()) {
                return ResponseEntity.ok(new KanbanElementContentResponse(content.get(), zoneId));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT), HttpStatus.NOT_FOUND);
        }
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
                            schema = @Schema(implementation = KanbanElementContentResponse.class))
            })
    })
    @PostMapping("/add")
    public ResponseEntity<?> addElement(@RequestBody @Valid CreateKanbanElementRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                String login = provider.getLoginFromToken();
                Optional<KanbanElement> element = kanbanElementService.addElement(request, login);
                if (element.isPresent()) {
                    return ResponseEntity.ok(new KanbanElementContentResponse(element.get(), 0));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COLUMN), HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Изменение элемента", description = "Обновление всех текстовых полей элемента")
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
    @PutMapping("/put")
    public ResponseEntity<?> editElement(@RequestParam long id, @RequestBody @Valid UpdateKanbanElementRequest request,
                                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                Optional<KanbanElement> element = kanbanElementService.setElement(id, request,
                        provider.getLoginFromToken());
                if (element.isPresent()) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT), HttpStatus.NOT_FOUND);
            } catch (IncorrectStatusException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                        HttpStatus.GONE);
            }
        }
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
    @DeleteMapping("/delete")
    public ResponseEntity<?> removeElement(@RequestParam long id) {
        try {
            String login = provider.getLoginFromToken();
            Optional<KanbanColumn> column = kanbanElementService.utilizeElementFromUser(id, login);
            if (column.isPresent()) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT), HttpStatus.NOT_FOUND);
        } catch (IncorrectStatusException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                    HttpStatus.GONE);
        }
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
                                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(
                    new ErrorResponse(bindingResult.getAllErrors().stream()
                            .map(ObjectError::getDefaultMessage)
                            .map(Errors::valueOf)
                            .map(Errors::getNumValue)
                            .collect(Collectors.toList())),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (kanbanElementService.transportElement(transportElementRequest, provider.getLoginFromToken())) {
                    return new ResponseEntity<>(HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT_OR_COLUMN),
                        HttpStatus.NOT_FOUND);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.INDEX_MORE_COLLECTION_SIZE),
                        HttpStatus.BAD_REQUEST);
            } catch (IncorrectStatusException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                        HttpStatus.GONE);
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
            @ApiResponse(responseCode = "200", description = "Тег успешно добавлен"),
    })
    @PostMapping("/tag")
    public ResponseEntity<?> addTag(@RequestParam long elementId, @RequestParam long tagId){
        try{
            if(kanbanElementService.addTag(elementId, tagId, provider.getLoginFromToken())){
                return new ResponseEntity<>(HttpStatus.OK);
            } else{
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_TAG), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Удаление тега из элемента")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему элементу",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к данному ресурсу"),
            @ApiResponse(responseCode = "200", description = "Тег успешно удалён"),
    })
    @DeleteMapping("/tag")
    public ResponseEntity<?> removeTag(@RequestParam long elementId, @RequestParam long tagId){
        try{
            if(kanbanElementService.removeTag(elementId, tagId, provider.getLoginFromToken())){
                return new ResponseEntity<>(HttpStatus.OK);
            } else{
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT), HttpStatus.NOT_FOUND);
        }
    }
}
