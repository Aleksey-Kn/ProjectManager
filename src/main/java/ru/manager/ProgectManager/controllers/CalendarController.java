package ru.manager.ProgectManager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.calendar.CalendarResponseList;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.CalendarService;

import java.security.Principal;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/calendar")
@Tag(name = "Работа с календарём")
public class CalendarController {
    private final CalendarService calendarService;

    @Operation(summary = "Получение карточек канбана, принадлежащих к указанному месяцу")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карточки, принадлежащие к указанному месяцу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CalendarResponseList.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к указанному проекту"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping
    public ResponseEntity<?> findCalendar(@RequestParam int projectId, @RequestParam int year, @RequestParam int month,
                                          Principal principal) {
        try{
            Optional<CalendarResponseList> response =
                    calendarService.findCalendar(projectId, year, month, principal.getName());
            if(response.isPresent()) {
                return ResponseEntity.ok(response.get());
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
        }
    }
}
