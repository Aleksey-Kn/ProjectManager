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
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.AccessProjectRequest;
import ru.manager.ProgectManager.DTO.response.AccessProjectResponse;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.AccessProject;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.services.AccessProjectService;

import java.util.NoSuchElementException;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Предоставление доступа к проекту",
        description = "Позволяет приглашать пользователя в проект с помощью токена доступа")
public class AccessController {
    private final AccessProjectService accessProjectService;
    private final JwtProvider provider;

    @Operation(summary = "Получение доступа",
            description = "Предоставляет доступ к проекту, к которому относится токен, пользователю, перешедшуму по данной ссылке")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "403", description = "Некорректный или устаревший токен доступа к проекту",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200")
    })
    @GetMapping("/access")
    public ResponseEntity<?> getAccess(@RequestParam String token) {
        try {
            if (accessProjectService.createAccessForUser(token, provider.getLoginFromToken())) {
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(
                    new ErrorResponse(Errors.PROJECT_ACCESS_TOKEN_IS_DEPRECATED),
                    HttpStatus.FORBIDDEN);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(
                    new ErrorResponse(Errors.PROJECT_ACCESS_TOKEN_IS_INVALID_OR_NO_LONGER_AVAILABLE),
                    HttpStatus.FORBIDDEN);
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
            @ApiResponse(responseCode = "400", description = "Указанного проекта не существует", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            }),
            @ApiResponse(responseCode = "406",
                    description = "Попытка создать многоразовую ссылку для приглашения пользователя, как администратора",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccessProjectResponse.class))})
    })
    @PostMapping("/access")
    public ResponseEntity<?> postAccess(@RequestBody AccessProjectRequest accessProjectRequest) {
        try {
            Optional<AccessProject> accessProject = accessProjectService.generateTokenForAccessProject(provider.getLoginFromToken(),
                    accessProjectRequest.getProjectId(), accessProjectRequest.getTypeRoleProject(),
                    accessProjectRequest.getRoleId(), accessProjectRequest.isDisposable(),
                    accessProjectRequest.getLiveTimeInDays());
            return accessProject.map(s -> ResponseEntity.ok(new AccessProjectResponse(s)))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.FORBIDDEN));
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT),
                    HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(
                    new ErrorResponse(Errors.TOKEN_FOR_ACCESS_WITH_PROJECT_AS_ADMIN_MUST_BE_DISPOSABLE),
                    HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
