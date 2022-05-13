package ru.manager.ProgectManager.controllers.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.user.NoteRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.components.authorization.JwtProvider;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.NoteService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/user")
@Tag(name = "Заметки о пользователях")
public class NoteController {
    private final NoteService noteService;
    private final JwtProvider provider;

    @Operation(summary = "Создание или изменение заметки")
    @PostMapping("/note")
    public ResponseEntity<?> postNote(@RequestBody @Valid NoteRequest noteRequest, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.TEXT_MUST_BE_CONTAINS_VISIBLE_SYMBOL),
                    HttpStatus.BAD_REQUEST);
        } else {
            if(noteService.setNote(noteRequest.getText(), noteRequest.getTargetUserId(), provider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
        }
    }

    @Operation(summary = "Удаление заметки")
    @DeleteMapping("/note")
    public ResponseEntity<?> deleteNote(@RequestParam
                                            @Parameter(description = "Идентификатор пользователя, " +
                                                    "к которому приклеплёна удаляемая записка") long id) {
        if(noteService.deleteNote(id, provider.getLoginFromToken())) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
