package ru.manager.ProgectManager.controllers.kanban;

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
import ru.manager.ProgectManager.DTO.request.kanban.KanbanCommentRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.kanban.KanbanElementCommentResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.entitys.kanban.KanbanElementComment;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.IncorrectStatusException;
import ru.manager.ProgectManager.services.kanban.KanbanElementService;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/kanban/element/comment")
@Tag(name = "Манипуляции с комментариями в элементе канбана")
public class KanbanElementCommentController {
    private final KanbanElementService kanbanElementService;
    private final JwtProvider provider;

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
            }),
            @ApiResponse(responseCode = "410",
                    description = "Операция недоступна, поскольку элемент перемещён в корзину", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PostMapping("/add")
    public ResponseEntity<?> addComment(@RequestBody @Valid KanbanCommentRequest request, BindingResult bindingResult) {
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
                Optional<KanbanElementComment> comment = kanbanElementService.addComment(request, provider.getLoginFromToken());
                if (comment.isPresent()) {
                    return ResponseEntity.ok(new KanbanElementCommentResponse(comment.get(), request.getZone()));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT), HttpStatus.BAD_REQUEST);
            } catch (IncorrectStatusException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                        HttpStatus.GONE);
            }
        }
    }

    @Operation(summary = "Изменение комментария элемента канбана")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Обращение к несуществующему элементу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к комментарию"),
            @ApiResponse(responseCode = "406", description = "Неподходящие текстовые данные",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Полная информация о изменённом комментарии", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanElementCommentResponse.class))
            }),
            @ApiResponse(responseCode = "410",
                    description = "Операция недоступна, поскольку элемент перемещён в корзину", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PutMapping("/put")
    public ResponseEntity<?> updateComment(@RequestBody @Valid KanbanCommentRequest request, BindingResult bindingResult) {
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
                Optional<KanbanElementComment> comment = kanbanElementService.updateComment(request, provider.getLoginFromToken());
                if (comment.isPresent()) {
                    return ResponseEntity.ok(new KanbanElementCommentResponse(comment.get(), request.getZone()));
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COMMENT), HttpStatus.BAD_REQUEST);
            } catch (IncorrectStatusException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                        HttpStatus.GONE);
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
            @ApiResponse(responseCode = "200", description = "Комментарий успешно удалён"),
            @ApiResponse(responseCode = "410",
                    description = "Операция недоступна, поскольку элемент перемещён в корзину", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @DeleteMapping("/delete")
    public ResponseEntity<?> removeComment(@RequestParam long id) {
        try {
            Optional<KanbanElement> element = kanbanElementService.deleteComment(id, provider.getLoginFromToken());
            if (element.isPresent()) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_COMMENT), HttpStatus.BAD_REQUEST);
        } catch (IncorrectStatusException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                    HttpStatus.GONE);
        }
    }
}
