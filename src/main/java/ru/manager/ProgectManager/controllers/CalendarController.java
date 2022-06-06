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
import ru.manager.ProgectManager.DTO.response.calendar.ShortKanbanElementInfoList;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.CalendarService;

import java.security.Principal;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/calendar")
@Tag(name = "Работа с календарём")
public class CalendarController {
    private final CalendarService calendarService;

    @Operation(summary = "Получение карточек канбана из указанного проекта, принадлежащих к указанному месяцу")
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
    public ResponseEntity<?> findCalendar(@RequestParam long projectId, @RequestParam int year, @RequestParam int month,
                                          Principal principal) {
        try {
            Optional<CalendarResponseList> response =
                    calendarService.findCalendar(projectId, year, month, principal.getName());
            if (response.isPresent()) {
                return ResponseEntity.ok(response.get());
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Получение карточек указанного канбана, принадлежащих к указанному месяцу")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карточки, принадлежащие к указанному месяцу", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CalendarResponseList.class))
            }),
            @ApiResponse(responseCode = "403", description = "Пользователь не имеет доступа к указанному канбану"),
            @ApiResponse(responseCode = "404", description = "Указанного канбана не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/kanban")
    public ResponseEntity<?> findCalendarFromKanban(@RequestParam long id, @RequestParam int year,
                                                    @RequestParam int month, Principal principal) {
        try {
            Optional<CalendarResponseList> response =
                    calendarService.findCalendarOnKanban(id, year, month, principal.getName());
            if (response.isPresent()) {
                return ResponseEntity.ok(response.get());
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Получение карточек из всех доступных канбанов, выбранная дата которых равна указанному дню")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список информации о найденных карточках", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ShortKanbanElementInfoList.class))
            }),
            @ApiResponse(responseCode = "400", description = "Неверный формат даты", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            })
    })
    @GetMapping("/by_day")
    public ResponseEntity<?> findAllCardsByDay(@RequestParam String date, Principal principal) {
        try {
            return ResponseEntity.ok(calendarService.findTaskOnDay(date, principal.getName()));
        } catch (DateTimeParseException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.WRONG_DATE_FORMAT), HttpStatus.BAD_REQUEST);
        }
    }
}
