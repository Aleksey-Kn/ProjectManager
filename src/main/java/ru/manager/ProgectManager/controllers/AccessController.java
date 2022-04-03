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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.manager.ProgectManager.DTO.request.AccessProjectRequest;
import ru.manager.ProgectManager.DTO.request.CreateCustomRoleRequest;
import ru.manager.ProgectManager.DTO.request.EditUserRoleRequest;
import ru.manager.ProgectManager.DTO.response.AccessProjectResponse;
import ru.manager.ProgectManager.DTO.response.CustomProjectRoleResponseList;
import ru.manager.ProgectManager.DTO.response.ErrorResponse;
import ru.manager.ProgectManager.components.JwtProvider;
import ru.manager.ProgectManager.entitys.AccessProject;
import ru.manager.ProgectManager.entitys.CustomProjectRole;
import ru.manager.ProgectManager.enums.Errors;
import ru.manager.ProgectManager.exception.NoSuchResourceException;
import ru.manager.ProgectManager.services.AccessProjectService;

import javax.validation.Valid;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;


@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Предоставление доступа к проекту",
        description = "Позволяет приглашать пользователя в проект с помощью токена доступа")
public class AccessController {
    private final AccessProjectService accessProjectService;
    private final JwtProvider provider;

    @GetMapping("/roles")
    public ResponseEntity<?> findAllCustomRole(@RequestParam @Parameter(description = "Идентификатор проекта") long id){
        try {
            Optional<Set<CustomProjectRole>> roles = accessProjectService
                    .findAllCustomProjectRole(id, provider.getLoginFromToken());
            if(roles.isPresent()){
                return ResponseEntity.ok(new CustomProjectRoleResponseList(roles.get()));
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/role")
    public ResponseEntity<?> addRole(@RequestBody @Valid CreateCustomRoleRequest request, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.NOT_ACCEPTABLE);
        } else{
            try{
                if(accessProjectService.createCustomRole(request, provider.getLoginFromToken())){
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e){
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.BAD_REQUEST);
            } catch (NoSuchResourceException e){
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN), HttpStatus.BAD_REQUEST);
            }
        }
    }

    @DeleteMapping("/role")
    public ResponseEntity<?> deleteRole(@RequestParam long projectId, @RequestParam long roleId){
        try {
            if(accessProjectService.deleteCustomRole(projectId, roleId, provider.getLoginFromToken())){
                return new ResponseEntity<>(HttpStatus.OK);
            } else{
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CUSTOM_ROLE), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/role")
    public ResponseEntity<?> editRole(@RequestBody CreateCustomRoleRequest request, @RequestParam long roleId){
        try{
            if(accessProjectService.changeRole(roleId, request, provider.getLoginFromToken())){
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } catch (NoSuchElementException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.BAD_REQUEST);
        } catch (NoSuchResourceException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_KANBAN), HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e){
            return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CUSTOM_ROLE), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/edit_user_role")
    public ResponseEntity<?> editUserRole(@RequestBody @Valid EditUserRoleRequest request, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.NOT_ACCEPTABLE);
        } else {
            try {
                if (accessProjectService.editUserRole(request, provider.getLoginFromToken())) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            } catch (NoSuchElementException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_PROJECT), HttpStatus.BAD_REQUEST);
            } catch (NoSuchResourceException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_USER), HttpStatus.BAD_REQUEST);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(new ErrorResponse(Errors.NO_SUCH_SPECIFIED_CUSTOM_ROLE), HttpStatus.BAD_REQUEST);
            }
        }
    }

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
                    description = "Роль пользователя не указана или " +
                            "попытка создать многоразовую ссылку для приглашения пользователя, как администратора",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class))
                    }),
            @ApiResponse(responseCode = "200", content = {
                    @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccessProjectResponse.class))})
    })
    @PostMapping("/access")
    public ResponseEntity<?> postAccess(@RequestBody @Valid AccessProjectRequest accessProjectRequest,
                                        BindingResult bindingResult) {
        if(bindingResult.hasErrors()){
            return new ResponseEntity<>(new ErrorResponse(Errors.NAME_MUST_BE_CONTAINS_VISIBLE_SYMBOLS),
                    HttpStatus.NOT_ACCEPTABLE);
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
                        HttpStatus.BAD_REQUEST);
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>(
                        new ErrorResponse(Errors.TOKEN_FOR_ACCESS_WITH_PROJECT_AS_ADMIN_MUST_BE_DISPOSABLE),
                        HttpStatus.NOT_ACCEPTABLE);
            }
        }
    }
}
