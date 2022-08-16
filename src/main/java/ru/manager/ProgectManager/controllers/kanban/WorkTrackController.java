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
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.user.CreateWorkTrackRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.workTrack.AllWorkUserInfo;
import ru.manager.ProgectManager.components.ErrorResponseEntityConfigurator;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.ForbiddenException;
import ru.manager.ProgectManager.exception.kanban.IncorrectElementStatusException;
import ru.manager.ProgectManager.exception.kanban.NoSuchKanbanElementException;
import ru.manager.ProgectManager.exception.project.NoSuchProjectException;
import ru.manager.ProgectManager.exception.user.NoSuchUserException;
import ru.manager.ProgectManager.services.user.WorkTrackService;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/users/kanban/element/work")
@RequiredArgsConstructor
@Tag(name = "Манипуляции с трекингом времени работы")
public class WorkTrackController {
    private final WorkTrackService workTrackService;
    private final ErrorResponseEntityConfigurator entityConfigurator;

    @Operation(summary = "Добавление времени работы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Некорректные заначения полей запроса", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "403", description = "У пользователя нет доступа к данному действию"),
            @ApiResponse(responseCode = "404", description = "Указанного элемента не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "410",
                    description = "Указанный элемент перемещён в корзину и недоступен для изменения",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Время работы успешно добавлено")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> addWorkTrack(@RequestBody @Valid CreateWorkTrackRequest request,
                                          BindingResult bindingResult, Principal principal)
            throws ForbiddenException, IncorrectElementStatusException, NoSuchKanbanElementException {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            workTrackService.addWorkTrack(request, principal.getName());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    @Operation(summary = "Удаление времени работы")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "У пользователя нет доступа к данному действию"),
            @ApiResponse(responseCode = "404", description = "Указанной записи не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "410",
                    description = "Элемент канбана перемещён в корзину и недоступен для изменения",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Время работы успешно удалено")
    })
    @DeleteMapping
    public ResponseEntity<?> removeWorkTrack(@RequestParam @Parameter(description = "Идентификатор удаляемого времени работы")
                                             long id, Principal principal)
            throws ForbiddenException, IncorrectElementStatusException, NoSuchKanbanElementException {
        if (workTrackService.removeWorkTrack(id, principal.getName())) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_WORK_TRACK), HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Получение отчёта о работе участника проекта или о своей работе в проекте")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "У пользователя нет доступа к данному действию"),
            @ApiResponse(responseCode = "404", description = "Указанного проекта или пользователя не существует",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Отчёт о проделанной работе", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AllWorkUserInfo.class))
            })
    })
    @GetMapping
    public AllWorkUserInfo findWorkTracks(@RequestParam String fromDate, @RequestParam String toDate,
                                          @RequestParam long projectId, @RequestParam long userId,
                                          Principal principal)
            throws ForbiddenException, NoSuchProjectException, NoSuchUserException {
        return userId == 0
                ? workTrackService.findWorkTrackMyself(fromDate, toDate, projectId, principal.getName())
                : workTrackService.findOtherWorkTrackAsAdmin(fromDate, toDate, projectId, userId,
                principal.getName());
    }
}
