package ru.manager.ProgectManager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.calendar.CalendarResponseList;
import ru.manager.ProgectManager.DTO.response.calendar.ShortKanbanElementInfoList;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.kanban.NoSuchKanbanException;
import ru.manager.ProgectManager.exception.project.NoSuchProjectException;
import ru.manager.ProgectManager.services.CalendarService;

import java.security.Principal;
import java.time.format.DateTimeParseException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/calendar")
@Tag(name = "Работа с календарём")
public class CalendarController {
    private final CalendarService calendarService;

    @ExceptionHandler(DateTimeParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse dateTimeParseExceptionHandler() {
        return new ErrorResponse(Errors.WRONG_DATE_FORMAT);
    }

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
    public CalendarResponseList findCalendar(@RequestParam long projectId, @RequestParam int year,
                                             @RequestParam int month, Principal principal)
            throws ForbiddenException, NoSuchProjectException {
        return calendarService.findCalendar(projectId, year, month, principal.getName());
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
    public CalendarResponseList findCalendarFromKanban(@RequestParam long id, @RequestParam int year,
                                                       @RequestParam int month, Principal principal)
            throws ForbiddenException, NoSuchKanbanException {
        return calendarService.findCalendarOnKanban(id, year, month, principal.getName());
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
    public ShortKanbanElementInfoList findAllCardsByDay(@RequestParam String date, Principal principal) {
        return calendarService.findTaskOnDay(date, principal.getName());
    }
}
