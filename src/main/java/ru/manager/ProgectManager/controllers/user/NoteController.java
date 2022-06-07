package ru.manager.ProgectManager.controllers.user;

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
import ru.manager.ProgectManager.DTO.request.user.NoteRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.user.NoteService;

import javax.validation.Valid;
import java.security.Principal;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/user")
@Tag(name = "Заметки о пользователях")
public class NoteController {
    private final NoteService noteService;

    @Operation(summary = "Создание или изменение заметки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заметка успешно создана или обновлена"),
            @ApiResponse(responseCode = "400", description = "Текст заметки не должен быть пустым", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "404", description = "Указанного пользователя не существует"),
            @ApiResponse(responseCode = "409", description = "Нельзя присваивать заметку самому себе")
    })
    @PostMapping("/note")
    public ResponseEntity<?> postNote(@RequestBody @Valid NoteRequest noteRequest, BindingResult bindingResult,
                                      Principal principal) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.TEXT_MUST_BE_CONTAINS_VISIBLE_SYMBOL),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                if (noteService.setNote(noteRequest.getText(), noteRequest.getTargetUserId(), principal.getName())) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.CONFLICT);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
    }

    @Operation(summary = "Удаление заметки")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заметка успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Требуемой заметки не существует")
    })
    @DeleteMapping("/note")
    public ResponseEntity<?> deleteNote(@RequestParam @Parameter(description = "Идентификатор пользователя, " +
            "к которому приклеплёна удаляемая записка") long id, Principal principal) {
        if (noteService.deleteNote(id, principal.getName())) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
