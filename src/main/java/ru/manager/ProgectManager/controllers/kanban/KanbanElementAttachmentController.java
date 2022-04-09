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
import ru.manager.ProgectManager.DTO.response.kanban.AttachAllDataResponse;
import ru.manager.ProgectManager.DTO.response.kanban.AttachMainDataResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.kanban.KanbanAttachment;
import ru.manager.ProgectManager.entitys.kanban.KanbanElement;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.IncorrectStatusException;
import ru.manager.ProgectManager.services.kanban.KanbanElementService;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/kanban/element/attachment")
@Tag(name = "Манипуляции со вложениями")
public class KanbanElementAttachmentController {
    private final KanbanElementService kanbanElementService;
    private final JwtProvider provider;

    @Operation(summary = "Добавление вложения")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "404", description = "Обращение к несуществующему элементу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к проекту"),
            @ApiResponse(responseCode = "200", description = "Информация о добавленном вложении", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AttachMainDataResponse.class))
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
    @PostMapping("/add")
    public ResponseEntity<?> addAttachment(@RequestParam long id, @ModelAttribute PhotoDTO photoDTO) {
        try {
            Optional<KanbanAttachment> attachment =
                    kanbanElementService.addAttachment(id, provider.getLoginFromToken(), photoDTO.getFile());
            if (attachment.isPresent()) {
                return ResponseEntity.ok(new AttachMainDataResponse(attachment.get()));
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
    @GetMapping("/get")
    public ResponseEntity<?> getAttachment(@RequestParam long id) {
        try {
            Optional<KanbanAttachment> attachment = kanbanElementService.getAttachment(id, provider.getLoginFromToken());
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
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAttachment(@RequestParam long id) {
        try {
            Optional<KanbanElement> element = kanbanElementService.deleteAttachment(id, provider.getLoginFromToken());
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
