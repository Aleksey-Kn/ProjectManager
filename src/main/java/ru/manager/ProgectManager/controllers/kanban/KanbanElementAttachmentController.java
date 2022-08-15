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
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.PhotoDTO;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.IdResponse;
import ru.manager.ProgectManager.DTO.response.kanban.AttachAllDataResponse;
import ru.manager.ProgectManager.entitys.kanban.KanbanAttachment;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.runtime.IncorrectStatusException;
import ru.manager.ProgectManager.services.kanban.KanbanElementAttributesService;

import java.io.IOException;
import java.security.Principal;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/kanban/element/attachment")
@Tag(name = "Манипуляции со вложениями")
public class KanbanElementAttachmentController {
    private final KanbanElementAttributesService attributesService;

    @Operation(summary = "Добавление вложения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему элементу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "200", description = "Информация о добавленном вложении", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = IdResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Ошибка чтения файла", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "410",
                    description = "Операция недоступна, поскольку элемент перемещён в корзину", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @PostMapping()
    public ResponseEntity<?> addAttachment(@RequestParam long id, @ModelAttribute PhotoDTO photoDTO,
                                           Principal principal) {
        try {
            Optional<KanbanAttachment> attachment =
                    attributesService.addAttachment(id, principal.getName(), photoDTO.getFile());
            if (attachment.isPresent()) {
                return ResponseEntity.ok(new IdResponse(attachment.get().getId()));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT), HttpStatus.NOT_FOUND);
        } catch (IOException | NullPointerException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.BAD_FILE), HttpStatus.BAD_REQUEST);
        } catch (IncorrectStatusException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                    HttpStatus.GONE);
        }
    }

    @Operation(summary = "Получение вложения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему вложению", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "200", description = "Запрашиваемое вложение", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = KanbanAttachment.class))
            })
    })
    @GetMapping()
    public ResponseEntity<?> getAttachment(@RequestParam long id, Principal principal) {
        try {
            Optional<KanbanAttachment> attachment = attributesService.getAttachment(id, principal.getName());
            if (attachment.isPresent()) {
                return ResponseEntity.ok(new AttachAllDataResponse(attachment.get()));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ATTACHMENT), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Удаление вложения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему вложению", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "200", description = "Вложение успешно удалено"),
            @ApiResponse(responseCode = "410",
                    description = "Операция недоступна, поскольку элемент перемещён в корзину", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @DeleteMapping()
    public ResponseEntity<?> deleteAttachment(@RequestParam long id, Principal principal) {
        try {
            Optional<KanbanElement> element = attributesService.deleteAttachment(id, principal.getName());
            if (element.isPresent()) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ATTACHMENT), HttpStatus.NOT_FOUND);
        } catch (IncorrectStatusException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                    HttpStatus.GONE);
        }
    }
}
