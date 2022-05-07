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
import ru.manager.ProgectManager.DTO.request.WorkTrackRequest;
import ru.manager.ProgectManager.DTO.request.user.CreateWorkTrackRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.workTrack.AllWorkUserInfo;
import ru.manager.ProgectManager.components.ErrorResponseEntityConfigurator;
import ru.manager.ProgectManager.components.authorization.JwtProvider;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.IncorrectStatusException;
import ru.manager.ProgectManager.services.kanban.WorkTrackService;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/users/kanban/element/work")
@RequiredArgsConstructor
@Tag(name = "Манипуляции с трекингом времени работы")
public class WorkTrackController {
    private final WorkTrackService workTrackService;
    private final JwtProvider provider;
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
    @PostMapping("/add")
    public ResponseEntity<?> addWorkTrack(@RequestBody @Valid CreateWorkTrackRequest request,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            try {
                if (workTrackService.addWorkTrack(request, provider.getLoginFromToken())) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT), HttpStatus.NOT_FOUND);
            } catch (IncorrectStatusException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                        HttpStatus.GONE);
            }
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
    @DeleteMapping("/delete")
    public ResponseEntity<?> removeWorkTrack(@RequestParam @Parameter(description = "Идентификатор удаляемого времени работы")
                                                     long id) {
        try {
            if (workTrackService.removeWorkTrack(id, provider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_ELEMENT), HttpStatus.NOT_FOUND);
        } catch (IncorrectStatusException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.INCORRECT_STATUS_ELEMENT_FOR_THIS_ACTION),
                    HttpStatus.GONE);
        }
    }

    @Operation(summary = "Получение отчёта о работе участника проекта или о своей работе в проекте")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Некорректные заначения полей запроса", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
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
    @GetMapping("/get")
    public ResponseEntity<?> findWorkTracks(@RequestBody @Valid WorkTrackRequest request, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return entityConfigurator.createErrorResponse(bindingResult);
        } else {
            try {
                Optional<AllWorkUserInfo> response = (request.getUserId() == -1
                        ? workTrackService.findWorkTrackMyself(request.getFromDate(), request.getToDate(),
                        request.getProjectId(), provider.getLoginFromToken())
                        : workTrackService.findOtherWorkTrackAsAdmin(request.getFromDate(), request.getToDate(),
                        request.getProjectId(), request.getUserId(), provider.getLoginFromToken()));
                if (response.isPresent()) {
                    return ResponseEntity.ok(response.get());
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.NOT_FOUND);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_USER), HttpStatus.NOT_FOUND);
            }
        }
    }
}
