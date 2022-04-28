package ru.manager.ProgectManager.controllers;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.TestService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Функционал для удобства тестирования")
@RequestMapping("/test")
public class TestController {
    private final TestService testService;

    @Operation(summary = "Удаление пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно удалён"),
            @ApiResponse(responseCode = "404", description = "Указанного пользователя не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeUser(@RequestParam @Parameter(description = "Идентификатор удаляемого пользователя")
                                                String idOrLogin) {
        if (testService.removeUser(idOrLogin)) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_USER), HttpStatus.NOT_FOUND);
        }
    }
}
