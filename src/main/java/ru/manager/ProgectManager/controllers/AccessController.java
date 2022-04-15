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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.accessProject.AccessProjectRequest;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.DTO.response.ProjectResponse;
import ru.manager.ProgectManager.DTO.response.accessProject.AccessProjectResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.accessProject.AccessProject;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.AccessProjectService;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/users/access")
@Tag(name = "Предоставление доступа к проекту",
        description = "Позволяет приглашать пользователя в проект с помощью токена доступа")
public class AccessController {
    private final AccessProjectService accessProjectService;
    private final JwtProvider provider;

    @Operation(summary = "Получение доступа",
            description = "Предоставляет доступ к проекту, к которому относится токен, пользователю, перешедшуму по данной ссылке")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Устаревший токен доступа к проекту",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Некорректный токен доступа к проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200", description = "Подключение к проекту произошло успешно")
    })
    @GetMapping("/get")
    public ResponseEntity<?> getAccess(@RequestParam String token) {
        try {
            if (accessProjectService.createAccessForUser(token, provider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(
                        new ErrorResponse(Errors.PROJECT_ACCESS_TOKEN_IS_DEPRECATED),
                        HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.PROJECT_ACCESS_TOKEN_IS_INVALID_OR_NO_LONGER_AVAILABLE),
                    HttpStatus.FORBIDDEN);
        }
    }

    @Operation(summary = "Получение информации о проекте, к которому может быть предоставлен доступ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Устаревший токен доступа к проекту", content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Некорректный токен доступа к проекту", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "200",
                    description = "Информация о проекте, а также будущей роли пользователя в нём", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProjectResponse.class))
            })
    })
    @GetMapping("/info")
    public ResponseEntity<?> getInfo(@RequestParam String token) {
        try {
            Optional<ProjectResponse> response = accessProjectService.findInfoOfProjectFromAccessToken(token);
            if (response.isPresent()) {
                return ResponseEntity.ok(response.get());
            } else {
                return new ResponseEntity<>(new ErrorResponse(Errors.PROJECT_ACCESS_TOKEN_IS_DEPRECATED),
                        HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.PROJECT_ACCESS_TOKEN_IS_INVALID_OR_NO_LONGER_AVAILABLE),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Предоставление доступа", description = "Генерирует токен доступа к проекту")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403",
                    description = "Пользователь, пытающийся предоставить доступ, не является администратором проекта",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "404", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "400", description = "Роль пользователя не указана", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "406",
                    description = "Попытка создать многоразовую ссылку для приглашения пользователя, как администратора",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", description = "Создание токена доступа произошло успешно", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccessProjectResponse.class))})
    })
    @PostMapping("/create")
    public ResponseEntity<?> postAccess(@RequestBody @Valid AccessProjectRequest accessProjectRequest,
                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.BAD_REQUEST);
        } else {
            try {
                Optional<AccessProject> accessProject = accessProjectService.generateTokenForAccessProject(provider.getLoginFromToken(),
                        accessProjectRequest.getProjectId(), accessProjectRequest.getTypeRoleProject(),
                        accessProjectRequest.getRoleId(), accessProjectRequest.isDisposable(),
                        accessProjectRequest.getLiveTimeInDays());
                return accessProject.map(s -> ResponseEntity.ok(new AccessProjectResponse(s)))
                        .orElseGet(() -> new ResponseEntity<>(HttpStatus.FORBIDDEN));
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT),
                        HttpStatus.NOT_FOUND);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(
                        new ErrorResponse(Errors.TOKEN_FOR_ACCESS_WITH_PROJECT_AS_ADMIN_MUST_BE_DISPOSABLE),
                        HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }
}
